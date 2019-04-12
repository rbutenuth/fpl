package de.codecentric.fpl.datatypes.list;

import static java.lang.System.arraycopy;
import static java.util.Arrays.copyOf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.TunnelException;
import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.datatypes.FplLambda;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.Function;

// TODO: More operations:
// remove(int index)
// insert(FplValue value, int index),
// set/replace(FplValue value, int index)
// lowerHalf / upperHalf: split in two halfs, number of buckets power of 2, 
// so a divide and conquer algorithm is efficient

/**
 * An FPL List.
 */
public class FplList implements FplValue, Iterable<FplValue> {
	public static final FplList EMPTY_LIST = new FplList();

	private static final int BASE_SIZE = 8;
	private static final int FACTOR = 4;

	private final FplValue[][] shape;

	// private because there is EMPTY_LIST
	private FplList() {
		shape = new FplValue[0][];
	}

	private FplList(FplValue[][] data) {
		this.shape = data;
	}

	/**
	 * Create a list from one value
	 * 
	 * @param value The value
	 */
	public static FplList fromValue(FplValue value) {
		FplValue[][] data = new FplValue[1][];
		data[0] = bucket(value);
		return new FplList(data);
	}

	/**
	 * Create a list.
	 * 
	 * @param values Array with values, the values will NOT be copied, so don't
	 *               modify the array after calling this method!
	 */
	public static FplList fromValues(FplValue[] values) {
		if (values.length == 0) {
			return EMPTY_LIST;
		} else {
			FplValue[][] data = new FplValue[1][];
			data[0] = values;
			return new FplList(data);
		}
	}

	public static FplList fromValues(List<FplValue> list) {
		if (list.isEmpty()) {
			return EMPTY_LIST;
		} else {
			FplValue[] values = list.toArray(new FplValue[list.size()]);
			FplValue[][] data = new FplValue[1][];
			data[0] = values;
			return new FplList(data);
		}
	}

	public static FplList fromIterator(Iterator<FplValue> iter, int size) {
		FplValue[][] shape = createEmptyShape(size);
		for (int bucketsIdx = 0; bucketsIdx < shape.length; bucketsIdx++) {
			FplValue[] bucket = shape[bucketsIdx];
			for (int inBucketIdx = 0; inBucketIdx < bucket.length; inBucketIdx++) {
				bucket[inBucketIdx] = iter.next();
			}
		}
		return new FplList(shape);
	}

	public static FplList fromIterator(Iterator<FplValue> iter) {
		// TODO: Liste wie bei addAtEnd aufbauen, aber mit einem mutable shape.
		// (Letzter bucket nicht komplett gefüllt)
		// Erst nachdem der Iterator durchlaufen ist, den letzten bucket
		// auf die passende Länge kürzen.
		// Eventuell den letzten nicht bis 8 sondern bis 4*8=32 füllen
		// (seltener umkopieren).
		List<FplValue> values = new ArrayList<>();
		while (iter.hasNext()) {
			values.add(iter.next());
		}
		return FplList.fromValues(values);
	}

	/**
	 * Create a list.
	 * 
	 * @param values      Array with values, the values be copied.
	 * 
	 * @param bucketSizes The size of the used buckets. The of the sizes must match
	 *                    the length of <code>values</code>
	 */
	public static FplList fromValuesWithShape(FplValue[] values, int[] bucketSizes) {
		int sum = 0;
		for (int l : bucketSizes) {
			sum += l;
		}
		if (values.length != sum) {
			throw new IllegalArgumentException(
					"values.length = " + values.length + ", but sum of bucketSizes = " + sum);
		}
		FplValue[][] data = new FplValue[bucketSizes.length][];
		int i = 0;
		for (int bucketIdx = 0; bucketIdx < bucketSizes.length; bucketIdx++) {
			int size = bucketSizes[bucketIdx];
			data[bucketIdx] = new FplValue[size];
			arraycopy(values, i, data[bucketIdx], 0, size);
			i += size;
		}
		return new FplList(data);
	}

	/**
	 * @return First element of the list.
	 * @throws EvaluationException If list is empty.
	 */
	public FplValue first() throws EvaluationException {
		checkNotEmpty();
		return shape[0][0];
	}

	/**
	 * @return Last element of the list.
	 * @throws EvaluationException If list is empty.
	 */
	public FplValue last() throws EvaluationException {
		checkNotEmpty();
		FplValue[] lastBucket = shape[shape.length - 1];
		return lastBucket[lastBucket.length - 1];
	}

	/**
	 * @return Sublist without the first element.
	 * @throws EvaluationException If list is empty.
	 */
	public FplList removeFirst() throws EvaluationException {
		checkNotEmpty();
		if (shape[0].length == 1) {
			FplValue[][] bucketsDst = new FplValue[shape.length - 1][];
			arraycopy(shape, 1, bucketsDst, 0, bucketsDst.length);
			return new FplList(bucketsDst);
		}
		if (shape[0].length <= BASE_SIZE + 1) {
			FplValue[][] bucketsDst = new FplValue[shape.length][];
			arraycopy(shape, 1, bucketsDst, 1, bucketsDst.length - 1);
			bucketsDst[0] = new FplValue[shape[0].length - 1];
			arraycopy(shape[0], 1, bucketsDst[0], 0, bucketsDst[0].length);
			return new FplList(bucketsDst);
		}
		// First bucket is too large, split it according "ideal" shape
		int count = shape[0].length - 1;
		int additionalBuckets = -1;
		int bucketFillSize = BASE_SIZE / 2;
		while (count > 0) {
			additionalBuckets++;
			count -= bucketFillSize;
			bucketFillSize *= FACTOR;
		}

		FplValue[][] bucketsDst = new FplValue[shape.length + additionalBuckets][];
		bucketFillSize = BASE_SIZE / 2;
		bucketsDst[0] = new FplValue[bucketFillSize];
		arraycopy(shape[0], 1, bucketsDst[0], 0, bucketFillSize);

		int dstIdx = 1;
		count = shape[0].length - 1 - bucketFillSize;
		int inBucketIdx = bucketFillSize + 1;
		while (count > 0) {
			bucketFillSize *= FACTOR;
			if (bucketFillSize > count) {
				bucketFillSize = count;
			}
			bucketsDst[dstIdx] = new FplValue[bucketFillSize];
			arraycopy(shape[0], inBucketIdx, bucketsDst[dstIdx], 0, bucketFillSize);
			dstIdx++;
			inBucketIdx += bucketFillSize;
			count -= bucketFillSize;
		}
		int srcIdx = 1;
		while (dstIdx < bucketsDst.length) {
			bucketsDst[dstIdx++] = shape[srcIdx++];
		}
		return new FplList(bucketsDst);
	}

	/**
	 * @return Sublist without the last element.
	 * @throws EvaluationException If list is empty.
	 */
	public FplList removeLast() throws EvaluationException {
		checkNotEmpty();
		int lastIdx = shape.length - 1;
		if (shape[lastIdx].length == 1) {
			FplValue[][] bucketsDst = new FplValue[shape.length - 1][];
			arraycopy(shape, 0, bucketsDst, 0, bucketsDst.length);
			return new FplList(bucketsDst);
		}
		if (shape[lastIdx].length <= BASE_SIZE + 1) {
			FplValue[][] bucketsDst = new FplValue[shape.length][];
			arraycopy(shape, 0, bucketsDst, 0, bucketsDst.length - 1);
			bucketsDst[lastIdx] = new FplValue[shape[lastIdx].length - 1];
			arraycopy(shape[lastIdx], 0, bucketsDst[lastIdx], 0, bucketsDst[lastIdx].length);
			return new FplList(bucketsDst);
		}
		int count = shape[lastIdx].length - 1;
		int additionalBuckets = -1;
		int bucketFillSize = BASE_SIZE / 2;
		while (count > 0) {
			additionalBuckets++;
			count -= bucketFillSize;
			bucketFillSize *= FACTOR;
		}

		FplValue[][] bucketsDst = new FplValue[shape.length + additionalBuckets][];
		bucketFillSize = BASE_SIZE / 2;
		int dstIdx = shape.length + additionalBuckets - 1;
		int inBucketIdx = shape[lastIdx].length - bucketFillSize - 1;
		bucketsDst[dstIdx] = new FplValue[bucketFillSize];
		arraycopy(shape[lastIdx], inBucketIdx, bucketsDst[dstIdx], 0, bucketFillSize);

		dstIdx--;
		count = shape[lastIdx].length - 1 - bucketFillSize;
		while (count > 0) {
			bucketFillSize *= FACTOR;
			if (bucketFillSize > count) {
				bucketFillSize = count;
			}
			inBucketIdx -= bucketFillSize;
			bucketsDst[dstIdx] = new FplValue[bucketFillSize];
			arraycopy(shape[lastIdx], inBucketIdx, bucketsDst[dstIdx], 0, bucketFillSize);
			dstIdx--;
			count -= bucketFillSize;
		}
		lastIdx--;
		while (dstIdx >= 0) {
			bucketsDst[dstIdx--] = shape[lastIdx--];
		}
		return new FplList(bucketsDst);
	}

	/**
	 * @param position Position, starting with 0.
	 * @return Element at position.
	 * @throws EvaluationException If list is empty or if <code>position</code> &lt;
	 *                             0 or &gt;= {@link #size()}.
	 */
	public FplValue get(int position) throws EvaluationException {
		checkNotEmpty();
		if (position < 0) {
			throw new EvaluationException("position < 0");
		}
		int bucketIdx = 0;
		int count = 0;
		while (count + shape[bucketIdx].length <= position) {
			count += shape[bucketIdx].length;
			bucketIdx++;
			if (bucketIdx >= shape.length) {
				throw new EvaluationException("position >= size");
			}
		}
		return shape[bucketIdx][position - count];
	}

	/**
	 * Add one value as new first element of the list. (The "cons" of Lisp)
	 * 
	 * @param value Element to insert at front.
	 * @return New List: This list plus one new element at front.
	 */
	public FplList addAtStart(FplValue value) {
		if (isEmpty()) {
			return fromValue(value);
		}
		int bucketIdx;
		int carrySize;
		int maxSize = BASE_SIZE;
		if (shape[0].length + 1< maxSize) {
			// just add to first bucket
			bucketIdx = 1;
			carrySize = shape[0].length + 1;
		} else {
			bucketIdx = 0;
			carrySize = 1;
			while (bucketIdx < shape.length && carrySize + shape[bucketIdx].length >= maxSize && shape[bucketIdx].length < maxSize) {
				carrySize += shape[bucketIdx++].length;
				maxSize *= FACTOR;
			}
		}
		// buckedIdx points to the first bucket which is NOT part of the carry
		FplValue[][] bucketsDst = new FplValue[shape.length - bucketIdx + 1][];

		// Collect carry
		FplValue[] carry = new FplValue[carrySize];
		bucketsDst[0] = carry;
		carry[0] = value;
		for (int i = 0, dst = 1; i < bucketIdx; i++) {
			arraycopy(shape[i], 0, carry, dst, shape[i].length);
			dst += shape[i].length;
		}
		// Copy buckets (behind carry)
		if (bucketsDst.length - 1 > 0) {
			arraycopy(shape, bucketIdx, bucketsDst, 1, bucketsDst.length - 1);
		}

		return new FplList(bucketsDst);
	}

	/**
	 * Append one value at the end of the list.
	 * 
	 * @param value Element to be appended
	 * @return New List: This list plus the new element at the end.
	 */
	public FplList addAtEnd(FplValue value) {
		if (isEmpty()) {
			return fromValue(value);
		}
		int lastShapeIdx = shape.length - 1;
		int lastLength = shape[lastShapeIdx].length;
		if (lastLength < BASE_SIZE) {
			// Value fits into the last bucket
			FplValue[][] dstShape = new FplValue[shape.length][];
			arraycopy(shape, 0, dstShape, 0, shape.length - 1);
			FplValue[] lastBucket = copyOf(shape[lastShapeIdx], lastLength + 1);
			lastBucket[lastLength] = value;
			dstShape[lastShapeIdx] = lastBucket;
			return new FplList(dstShape);
		} else {
			// Value does not fit into the last bucket, create a new last bucket
			return addBucketAtEnd(bucket(value));
		}
	}

	private FplList addBucketAtEnd(FplValue[] bucket) {
		int lastShapeIdx = shape.length - 1;
		int lastCarryIdx = lastShapeIdx;
		int firstCarryIdx = lastCarryIdx - 1;
		int carrySize = shape[lastShapeIdx].length;
		int maxSize = BASE_SIZE * FACTOR;
		while (firstCarryIdx >= 0 // we are still within the array
				&& shape[firstCarryIdx].length + carrySize <= maxSize // carry fits in bucket
				&& shape[firstCarryIdx].length >= maxSize * FACTOR // next bucket is not too big
		// TODO: stop when buckets are getting smaller
		) {
			carrySize += shape[firstCarryIdx--].length;
			maxSize *= FACTOR;
		}
		firstCarryIdx++;

		FplValue[][] bucketsDst = new FplValue[firstCarryIdx + 2][];
		// Copy buckets (before carry)
		if (firstCarryIdx > 0) {
			arraycopy(shape, 0, bucketsDst, 0, firstCarryIdx);
		}
		// Collect carry
		FplValue[] carry = new FplValue[carrySize];
		for (int i = firstCarryIdx, dst = 0; i <= lastCarryIdx; i++) {
			arraycopy(shape[i], 0, carry, dst, shape[i].length);
			dst += shape[i].length;
		}
		bucketsDst[bucketsDst.length - 2] = carry;
		// Last bucket with the new element
		bucketsDst[bucketsDst.length - 1] = bucket;

		return new FplList(bucketsDst);
	}

	/**
	 * Append a second list to this list.
	 * 
	 * @param list List to append, <code>null</code> is the same as an empty list.
	 * @return This list with appended list.
	 */
	public FplList append(FplList list) {
		if (list == null || list.isEmpty()) {
			return this;
		}
		if (isEmpty()) {
			return list;
		}
		int totalSize = size() + list.size();
		int totalBuckets = shape.length + list.shape.length;

		FplValue[] lastBucket = shape[shape.length - 1];
		FplValue[] listFirstBucket = list.shape[0];

		if (lastBucket.length + listFirstBucket.length <= BASE_SIZE) {
			if (needsReshaping(totalBuckets - 1, totalSize)) {
				return new FplList(mergedShape(shape, list.shape, totalSize));
			} else {
				FplValue[][] buckets = new FplValue[shape.length + list.shape.length - 1][];
				arraycopy(shape, 0, buckets, 0, shape.length - 1);
				FplValue[] bucket = new FplValue[lastBucket.length + listFirstBucket.length];
				arraycopy(lastBucket, 0, bucket, 0, lastBucket.length);
				arraycopy(listFirstBucket, 0, bucket, lastBucket.length, listFirstBucket.length);
				buckets[shape.length - 1] = bucket;
				arraycopy(list.shape, 1, buckets, shape.length, list.shape.length - 1);
				return new FplList(buckets);
			}
		} else {
			if (needsReshaping(totalBuckets, totalSize)) {
				return new FplList(mergedShape(shape, list.shape, totalSize));
			} else {
				FplValue[][] buckets = copyOf(shape, shape.length + list.shape.length);
				arraycopy(list.shape, 0, buckets, shape.length, list.shape.length);
				return new FplList(buckets);
			}
		}
	}

	private boolean needsReshaping(int numberOfBuckets, int size) {
		return (1 << numberOfBuckets) > size;
	}

	private FplValue[][] mergedShape(FplValue[][] left, FplValue[][] right, int totalSize) {
		FplValue[][] buckets = createEmptyShape(totalSize);

		int idx = 0, inBucketIdx = 0, dstIdx = 0, inBucketDstIdx = 0;
		// Copy entries from "left"
		while (idx < left.length) {
			int length = Math.min(left[idx].length - inBucketIdx, buckets[dstIdx].length - inBucketDstIdx);
			arraycopy(left[idx], inBucketIdx, buckets[dstIdx], inBucketDstIdx, length);
			inBucketIdx += length;
			if (inBucketIdx == left[idx].length) {
				inBucketIdx = 0;
				idx++;
			}
			inBucketDstIdx += length;
			if (inBucketDstIdx == buckets[dstIdx].length) {
				inBucketDstIdx = 0;
				dstIdx++;
			}
		}
		// Copy entries from "right"
		idx = 0;
		inBucketIdx = 0;
		while (idx < right.length) {
			int length = Math.min(right[idx].length - inBucketIdx, buckets[dstIdx].length - inBucketDstIdx);
			arraycopy(right[idx], inBucketIdx, buckets[dstIdx], inBucketDstIdx, length);
			inBucketIdx += length;
			if (inBucketIdx == right[idx].length) {
				inBucketIdx = 0;
				idx++;
			}
			inBucketDstIdx += length;
			if (inBucketDstIdx == buckets[dstIdx].length) {
				inBucketDstIdx = 0;
				dstIdx++;
			}
		}

		return buckets;
	}

	/**
	 * Create an empty shape starting at both ends with size 3/4 * BASE_SIZE and
	 * increasing by FACTOR to the middle.
	 * 
	 * @param size It place for this number of values.
	 * @return Array of arrays, all values <code>null</code>
	 */
	private static FplValue[][] createEmptyShape(int size) {
		int numBuckets = 2;
		int bucketSize = 3 * BASE_SIZE / 4;
		int sizeInBuckets = 2 * bucketSize;
		while (sizeInBuckets < size) {
			bucketSize *= FACTOR;
			sizeInBuckets += 2 * bucketSize;
			numBuckets += 2;
		}
		numBuckets--;
		FplValue[][] emptyShape = new FplValue[numBuckets][];
		bucketSize = BASE_SIZE;
		int rest = size;
		int i = 0, j = numBuckets - 1;
		while (i < j) {
			emptyShape[i] = new FplValue[bucketSize / 2];
			emptyShape[j] = new FplValue[bucketSize / 2];
			rest -= emptyShape[i].length + emptyShape[j].length;
			bucketSize *= FACTOR;
			i++;
			j--;
		}
		emptyShape[i] = new FplValue[rest];

		return emptyShape;
	}

	/**
	 * Returns a portion of this list between the specified {@code fromIndex},
	 * inclusive, and {@code toIndex}, exclusive. (If {@code fromIndex} and
	 * {@code toIndex} are equal, the returned list is empty.)
	 */
	public FplList subList(int fromIndex, int toIndex) throws EvaluationException {
		if (fromIndex < 0) {
			throw new EvaluationException("fromIndex < 0");
		}
		if (fromIndex > toIndex) {
			throw new EvaluationException("fromIndex > toIndex");
		}
		if (fromIndex == toIndex) {
			return EMPTY_LIST;
		}
		int bucketFromIdx = 0;
		int index = 0;

		// Determine index of first bucket and index within that bucket
		while (index + shape[bucketFromIdx].length <= fromIndex) {
			index += shape[bucketFromIdx].length;
			bucketFromIdx++;
			if (bucketFromIdx >= shape.length) {
				throw new EvaluationException("fromIndex >= size");
			}
		}
		int inBucketFromIdx = fromIndex - index;
		int bucketToIdx = bucketFromIdx;

		// Determine index of last bucket and index within that bucket
		while (index + shape[bucketToIdx].length <= toIndex - 1) {
			index += shape[bucketToIdx].length;
			bucketToIdx++;
			if (bucketToIdx >= shape.length) {
				throw new EvaluationException("toIndex > size + 1");
			}
		}
		int inBucketToIdx = toIndex - index;

		// Optimization: Return origin list when subList of complete list is requested
		if (fromIndex == 0 && bucketToIdx == shape.length - 1 && inBucketToIdx == shape[bucketToIdx].length) {
			return this;
		}

		if (bucketFromIdx == bucketToIdx) {
			return subListFromOneLargeArray(shape[bucketFromIdx], inBucketFromIdx, inBucketToIdx);
		} else {
			int numBucketsLeft = computeNumberOfBucketsLeft(shape[bucketFromIdx], inBucketFromIdx);
			int numBucketsRight = computeNumberOfBucketsRight(shape[bucketToIdx], inBucketToIdx);
			int numBucketsCenter = bucketToIdx - bucketFromIdx - 1;

			FplValue[][] bucketsDst = new FplValue[numBucketsLeft + numBucketsCenter + numBucketsRight][];

			createAndFillShapeFromLeft(shape[bucketFromIdx], inBucketFromIdx, bucketsDst);
			arraycopy(shape, bucketFromIdx + 1, bucketsDst, numBucketsLeft, numBucketsCenter);
			createAndFillShapeFromRight(shape[bucketToIdx], inBucketToIdx, bucketsDst);

			return new FplList(bucketsDst);
		}
	}

	private void createAndFillShapeFromLeft(FplValue[] bucket, int inBucketFromIdx, FplValue[][] bucketsDst) {
		if (inBucketFromIdx == 0) {
			bucketsDst[0] = bucket;
		} else {
			int bucketSize = 3 * BASE_SIZE / 4;
			int bucketDstIndex = 0;
			int rest = bucket.length - inBucketFromIdx;
			while (rest > bucketSize) {
				int size = Math.min(bucketSize / 2, rest);
				bucketsDst[bucketDstIndex] = new FplValue[size];
				arraycopy(bucket, inBucketFromIdx, bucketsDst[bucketDstIndex], 0, size);
				bucketDstIndex++;
				inBucketFromIdx += size;
				rest -= size;
				bucketSize *= FACTOR;
			}
			bucketsDst[bucketDstIndex] = new FplValue[rest];
			arraycopy(bucket, inBucketFromIdx, bucketsDst[bucketDstIndex], 0, rest);
		}
	}

	private void createAndFillShapeFromRight(FplValue[] bucket, int inBucketToIdx, FplValue[][] bucketsDst) {
		if (inBucketToIdx == bucket.length) {
			bucketsDst[bucketsDst.length - 1] = bucket;
		} else {
			int bucketSize = 3 * BASE_SIZE / 4;
			int bucketDstIndex = bucketsDst.length - 1;
			int rest = inBucketToIdx + 1;
			while (rest > bucketSize) {
				int size = Math.min(bucketSize / 2, rest);
				bucketsDst[bucketDstIndex] = new FplValue[size];
				arraycopy(bucket, inBucketToIdx - size + 1, bucketsDst[bucketDstIndex], 0, size);
				bucketDstIndex--;
				inBucketToIdx += size;
				rest -= size;
				bucketSize *= FACTOR;
			}
			bucketsDst[bucketDstIndex] = new FplValue[rest];
			arraycopy(bucket, 0, bucketsDst[bucketDstIndex], 0, rest);
		}
	}

	private int computeNumberOfBucketsLeft(FplValue[] fplValues, int inBucketIdx) {
		if (inBucketIdx == 0) {
			return 1; // It is possible to copy the complete bucket by reference
		}
		return numBucketsForCount(fplValues.length - inBucketIdx);
	}

	private int computeNumberOfBucketsRight(FplValue[] fplValues, int inBucketIdx) {
		if (inBucketIdx == fplValues.length) {
			return 1; // It is possible to copy the complete bucket by reference
		}
		return numBucketsForCount(inBucketIdx);
	}

	private int numBucketsForCount(int count) {
		if (count < BASE_SIZE) {
			return 1;
		}
		int rest = count;
		int bucketSize = 3 * BASE_SIZE / 4;
		int buckets = 1;
		while (rest > bucketSize) {
			rest -= bucketSize / 2; // fill to half
			bucketSize *= FACTOR;
			buckets++;
		}
		return buckets;
	}

	private FplList subListFromOneLargeArray(FplValue[] fplValues, int first, int behindLast) {
		int size = behindLast - first;
		if (size <= BASE_SIZE) {
			FplValue[] b = new FplValue[size];
			arraycopy(fplValues, first, b, 0, size);
			return FplList.fromValues(b);
		} else {
			FplValue[][] bucketsDst = createEmptyShape(size);
			for (int i = 0, bucketIdx = 0; bucketIdx < bucketsDst.length; bucketIdx++) {
				FplValue[] bucketDst = bucketsDst[bucketIdx];
				arraycopy(fplValues, first + i, bucketDst, 0, bucketDst.length);
				i += bucketDst.length;
			}
			return new FplList(bucketsDst);
		}
	}

	/**
	 * @return Number of elements in the list.
	 */
	public int size() {
		int count = 0;
		for (FplValue[] bucket : shape) {
			count += bucket.length;
		}
		return count;
	}

	/**
	 * Return an {@link Iterator} over this list, where each element is processed by
	 * a function before it is returned.
	 * 
	 * @param scope    Scope for evaluation of function
	 * @param function Lambda to apply.
	 * @return processed elements
	 * @throws TunnelException With a wrapped {@link EvaluationException} when
	 *                         function apply fails.
	 */
	public Iterator<FplValue> lambdaIterator(Scope scope, FplLambda function) {
		return new Iterator<FplValue>() {
			Iterator<FplValue> iter = iterator();

			@Override
			public boolean hasNext() {
				return iter.hasNext();
			}

			@Override
			public FplValue next() {
				try {
					return function.call(scope, new FplValue[] { iter.next() });
				} catch (EvaluationException e) {
					throw new TunnelException(e);
				}
			}
		};
	}

	@Override
	public Iterator<FplValue> iterator() {
		return new Iterator<FplValue>() {
			private int bucketsIdx = 0;
			private int inBucketIdx = 0;
			private boolean atEnd = isEmpty();

			@Override
			public boolean hasNext() {
				return !atEnd;
			}

			@Override
			public FplValue next() {
				if (atEnd) {
					throw new NoSuchElementException();
				}
				FplValue result = shape[bucketsIdx][inBucketIdx];
				inBucketIdx++;
				if (inBucketIdx == shape[bucketsIdx].length) {
					bucketsIdx++;
					inBucketIdx = 0;
					atEnd = bucketsIdx == shape.length;
				}
				return result;
			}
		};
	}

	@Override
	public FplValue evaluate(Scope scope) throws EvaluationException {
		if (isEmpty()) {
			return this; // empty list evaluates to empty list
		}
		FplValue firstElement = first().evaluate(scope);

		if (firstElement instanceof Function) {
			return ((Function) firstElement).call(scope, createParameterArray());
		} else {
			throw new EvaluationException("Not a function: " + firstElement);
		}
	}

	@Override
	public String typeName() {
		return "list";
	}

	private FplValue[] createParameterArray() {
		FplValue[] params;
		params = new FplValue[size() - 1];

		// find start indexes
		int bucketIdx = 0;
		int inBucketIdx = 1;
		if (inBucketIdx == shape[bucketIdx].length) {
			inBucketIdx = 0;
			bucketIdx++;
		}

		// copy values
		for (int i = 0; i < params.length; i++) {
			params[i] = shape[bucketIdx][inBucketIdx];
			inBucketIdx++;
			if (inBucketIdx == shape[bucketIdx].length) {
				inBucketIdx = 0;
				bucketIdx++;
			}
		}
		return params;
	}

	public boolean isEmpty() {
		return shape.length == 0;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append('(');
		Iterator<FplValue> iter = iterator();
		while (iter.hasNext()) {
			sb.append(iter.next().toString());
			if (iter.hasNext()) {
				sb.append(" ");
			}
		}
		sb.append(')');
		return sb.toString();
	}

	// only for testing
	int[] bucketSizes() {
		int[] sizes = new int[shape.length];
		for (int i = 0; i < shape.length; i++) {
			sizes[i] = shape[i].length;
		}
		return sizes;
	}

	private void checkNotEmpty() throws EvaluationException {
		if (isEmpty()) {
			throw new EvaluationException("List is empty");
		}
	}

	private static FplValue[] bucket(FplValue element) {
		FplValue[] result = new FplValue[1];
		result[0] = element;
		return result;
	}
}
