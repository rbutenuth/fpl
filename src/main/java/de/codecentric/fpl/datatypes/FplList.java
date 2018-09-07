package de.codecentric.fpl.datatypes;

import static java.lang.System.arraycopy;
import static java.util.Arrays.copyOf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.data.PositionHolder;
import de.codecentric.fpl.data.Scope;

// TODO: More operations: remove(int index), insert(FplValue value, int index),
// set(FplValue value, int index)

/**
 * An FPL List.
 */
public class FplList implements FplValue, Iterable<FplValue> {
	public static final FplList EMPTY_LIST = new FplList();

	private static final int BASE_SIZE = 8;
	private static final int FACTOR = 4;

	private final FplValue[] linear;
	private final FplValue[][] shape;

	// private because there is EMPTY_LIST
	private FplList() {
		linear = new FplValue[0];
		shape = null;
	}

	// Call this only when where are more than BASE_SIZE values in data!
	private FplList(FplValue[][] data) {
		linear = null;
		this.shape = data;
	}

	/**
	 * Create a list from one value
	 * 
	 * @param value
	 *            The value
	 */
	public FplList(FplValue value) {
		linear = bucket(value);
		shape = null;
	}

	/**
	 * Create a list.
	 * 
	 * @param values
	 *            Array with values, the values will NOT be copied, so don't modify
	 *            the array after calling this method!
	 */
	public FplList(FplValue[] values) {
		if (values.length <= BASE_SIZE) {
			linear = values;
			shape = null;
		} else {
			linear = null;
			shape = new FplValue[1][];
			shape[0] = values;
		}
	}

	// TODO: Do we really need this method?
	public FplList(Iterator<FplValue> iter) {
		ArrayList<FplValue> list = new ArrayList<>();
		while (iter.hasNext()) {
			list.add(iter.next());
		}
		FplValue[] values = list.toArray(new FplValue[list.size()]);
		if (values.length <= BASE_SIZE) {
			linear = values;
			shape = null;
		} else {
			linear = null;
			shape = new FplValue[1][];
			shape[0] = values;
		}
	}

	public FplList(List<FplValue> list) {
		FplValue[] values = list.toArray(new FplValue[list.size()]);
		if (values.length <= BASE_SIZE) {
			linear = values;
			shape = null;
		} else {
			linear = null;
			shape = new FplValue[1][];
			shape[0] = values;
		}
	}

	/**
	 * @return First element of the list.
	 * @throws EvaluationException
	 *             If list is empty.
	 */
	public FplValue first() throws EvaluationException {
		checkNotEmpty();
		if (linear == null) {
			return shape[0][0];
		} else {
			return linear[0];
		}
	}

	/**
	 * @return Last element of the list.
	 * @throws EvaluationException
	 *             If list is empty.
	 */
	public FplValue last() throws EvaluationException {
		checkNotEmpty();
		if (linear == null) {
			FplValue[] lastBucket = shape[shape.length - 1];
			return lastBucket[lastBucket.length - 1];
		} else {
			return linear[linear.length - 1];
		}
	}

	/**
	 * @return Sublist without the first element.
	 * @throws EvaluationException
	 *             If list is empty.
	 */
	public FplList removeFirst() throws EvaluationException {
		checkNotEmpty();
		if (linear == null) {
			return removeFirstShaped();
		} else {
			return removeFirstLinear();
		}
	}

	private FplList removeFirstLinear() {
		if (linear.length == 1) {
			return EMPTY_LIST;
		} else {
			FplValue[] values = new FplValue[linear.length - 1];
			arraycopy(linear, 1, values, 0, values.length);
			return new FplList(values);
		}
	}

	private FplList removeFirstShaped() {
		if (shape[0].length == 1) {
			FplValue[][] bucketsDst = new FplValue[shape.length - 1][];
			arraycopy(shape, 1, bucketsDst, 0, bucketsDst.length);
			return new FplList(bucketsDst);
		}
		if (shape[0].length <= BASE_SIZE) {
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
	 * @throws EvaluationException
	 *             If list is empty.
	 */
	public FplList removeLast() throws EvaluationException {
		checkNotEmpty();
		if (linear == null) {
			return removeLastShaped();
		} else {
			return removeLastLinear();
		}
	}

	private FplList removeLastLinear() throws EvaluationException {
		if (linear.length == 1) {
			return EMPTY_LIST;
		} else {
			FplValue[] values = new FplValue[linear.length - 1];
			arraycopy(linear, 0, values, 0, values.length);
			return new FplList(values);
		}
	}

	private FplList removeLastShaped() throws EvaluationException {
		int lastIdx = shape.length - 1;
		if (shape[lastIdx].length == 1) {
			FplValue[][] bucketsDst = new FplValue[shape.length - 1][];
			arraycopy(shape, 0, bucketsDst, 0, bucketsDst.length);
			return new FplList(bucketsDst);
		}
		if (shape[lastIdx].length <= BASE_SIZE + BASE_SIZE * FACTOR) {
			FplValue[][] bucketsDst = new FplValue[shape.length][];
			arraycopy(shape, 0, bucketsDst, 0, bucketsDst.length - 1);
			bucketsDst[lastIdx] = new FplValue[shape[lastIdx].length - 1];
			arraycopy(shape[0], 0, bucketsDst[0], 0, bucketsDst[lastIdx].length);
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
	 * @param position
	 *            Position, starting with 0.
	 * @return Element at position.
	 * @throws EvaluationException
	 *             If list is empty or if <code>position</code> &lt; 0 or &gt;=
	 *             {@link #size()}.
	 */
	public FplValue get(int position) throws EvaluationException {
		checkNotEmpty();
		if (linear == null) {
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
		} else {
			return linear[position];
		}
	}

	/**
	 * Add one value as new first element of the list. (The "cons" of Lisp)
	 * 
	 * @param value
	 *            Element to insert at front.
	 * @return New List: This list plus one new element at front.
	 */
	public FplList addAtStart(FplValue value) {
		if (linear == null) {
			return addAtStartShaped(value);
		} else {
			return addAtStartLinear(value);
		}
	}

	private FplList addAtStartLinear(FplValue value) {
		if (linear.length < BASE_SIZE) {
			FplValue[] destLinear = new FplValue[linear.length + 1];
			arraycopy(linear, 0, destLinear, 1, linear.length);
			destLinear[0] = value;
			return new FplList(destLinear);
		} else {
			FplValue[][] dstShape = new FplValue[2][];
			dstShape[0] = bucket(value);
			dstShape[1] = linear;
			return new FplList(dstShape);
		}
	}

	private FplList addAtStartShaped(FplValue value) {
		int firstLength = shape[0].length;
		if (firstLength < BASE_SIZE) {
			// Value fits into the first bucket
			FplValue[][] dstShape = new FplValue[shape.length][];
			arraycopy(shape, 1, dstShape, 1, shape.length - 1);
			FplValue[] firstBucket = new FplValue[firstLength + 1];
			arraycopy(shape[0], 0, firstBucket, 1, firstLength);
			firstBucket[0] = value;
			dstShape[0] = firstBucket;
			return new FplList(dstShape);
		} else {
			// Value does not fit into the first bucket, create a new first bucket
			return addBucketAtStart(bucket(value));
		}
	}

	private FplList addBucketAtStart(FplValue[] bucket) {
		int lastCarryIdx = 1;
		int carrySize = shape[0].length;
		int maxSize = BASE_SIZE * FACTOR;
		while (lastCarryIdx < shape.length // we are still within the array
				&& shape[lastCarryIdx].length + carrySize <= maxSize // carry fits in bucket
				&& shape[lastCarryIdx].length <= maxSize // next bucket is not too big
		// TODO: stop when buckets are getting smaller
		) {
			carrySize += shape[lastCarryIdx++].length;
			maxSize *= FACTOR;
		}
		lastCarryIdx--;

		FplValue[][] bucketsDst = new FplValue[shape.length - lastCarryIdx + 1][];
		// First bucket with the new element
		bucketsDst[0] = bucket;
		// Collect carry
		FplValue[] carry = new FplValue[carrySize];
		for (int i = 0, dst = 0; i <= lastCarryIdx; i++) {
			arraycopy(shape[i], 0, carry, dst, shape[i].length);
			dst += shape[i].length;
		}
		bucketsDst[1] = carry;
		// Copy buckets (behind carry)
		if (bucketsDst.length - 2 > 0) {
			arraycopy(shape, lastCarryIdx + 1, bucketsDst, 2, bucketsDst.length - 2);
		}

		return new FplList(bucketsDst);
	}

	/**
	 * Append one value at the end of the list.
	 * 
	 * @param value
	 *            Element to be appended
	 * @return New List: This list plus the new element at the end.
	 */
	public FplList addAtEnd(FplValue value) {
		if (linear == null) {
			return addAtEndShaped(value);
		} else {
			return addAtEndLinear(value);
		}
	}

	private FplList addAtEndLinear(FplValue value) {
		if (linear.length < BASE_SIZE) {
			FplValue[] destLinear = copyOf(linear, linear.length + 1);
			destLinear[linear.length] = value;
			return new FplList(destLinear);
		} else {
			FplValue[][] dstShape = new FplValue[2][];
			dstShape[0] = linear;
			dstShape[1] = bucket(value);
			return new FplList(dstShape);
		}
	}

	private FplList addAtEndShaped(FplValue value) {
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
				&& shape[firstCarryIdx].length <= maxSize // next bucket is not too big
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
	 * @param list
	 *            List to append, <code>null</code> is the same as an empty list.
	 * @return This list with appended list.
	 */
	public FplList append(FplList list) {
		if (list == null) {
			return this;
		}
		if (linear == null) {
			if (list.linear == null) {
				return appendShapedShaped(list);
			} else {
				return appendShapedLinear(list);
			}
		} else {
			if (list.linear == null) {
				return appendLinearShaped(list);
			} else {
				return appendLinearLinear(list);
			}
		}
	}

	private FplList appendShapedShaped(FplList list) {
		FplValue[][] buckets = copyOf(shape, shape.length + list.shape.length);
		arraycopy(list.shape, 0, buckets, shape.length, list.shape.length);
		// TODO: rehape!
		return new FplList(buckets);
	}

	private FplList appendShapedLinear(FplList list) {
		// Second list may be empty
		if (list.linear.length == 0) {
			return this;
		}
		FplValue[] lastBucket = shape[shape.length - 1];
		if (lastBucket.length + list.linear.length > BASE_SIZE) {
			return addBucketAtEnd(list.linear);
		} else {
			return null;
		}
	}

	private FplList appendLinearShaped(FplList list) {
		// First list may be empty
		if (linear.length == 0) {
			return list;
		}
		FplValue[] firstBucket = list.shape[0];
		if (firstBucket.length + linear.length > BASE_SIZE) {
			return list.addBucketAtStart(linear);
		} else {
			return null;
		}
	}

	private FplList appendLinearLinear(FplList list) {
		int size = linear.length + list.linear.length;
		if (size <= BASE_SIZE) {
			FplValue[] bucket = copyOf(linear, size);
			arraycopy(list.linear, 0, bucket, linear.length, list.linear.length);
			return new FplList(bucket);
		} else {
			FplValue[][] buckets = new FplValue[2][];
			buckets[0] = linear;
			buckets[1] = list.linear;
			return new FplList(buckets);
		}
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
		if (linear == null) {
			return subListShaped(fromIndex, toIndex);
		} else {
			FplValue[] values = new FplValue[toIndex - fromIndex];
			arraycopy(linear, fromIndex, values, 0, toIndex - fromIndex);
			return new FplList(values);
		}
	}

	private FplList subListShaped(int fromIndex, int toIndex) throws EvaluationException {
		int bucketFromIdx = 0;
		int count = 0;
		int lastCount = 0;
		while (count + shape[bucketFromIdx].length <= fromIndex) {
			lastCount = count;
			count += shape[bucketFromIdx].length;
			bucketFromIdx++;
			if (bucketFromIdx >= shape.length) {
				throw new EvaluationException("fromIndex >= size");
			}
		}
		int inBucketFromIdx = fromIndex - count;

		int bucketToIdx = bucketFromIdx;
		count = lastCount;
		while (count + shape[bucketToIdx].length <= toIndex - 1) {
			count += shape[bucketToIdx].length;
			bucketToIdx++;
			if (bucketToIdx >= shape.length) {
				throw new EvaluationException("toIndex >= size + 1");
			}
		}
		int inBucketToIdx = toIndex - 1 - count;

		// Optimization: Return origin list when subList of complete list is requested
		if (fromIndex == 0 && bucketToIdx == shape.length - 1 && inBucketToIdx == shape[bucketToIdx].length - 1) {
			return this;
		}

		if (bucketFromIdx == bucketToIdx) {
			return subList(shape[bucketFromIdx], inBucketFromIdx, inBucketToIdx);
		}
		int numBucketsLeft = computeNumberOfBucketsLeft(shape[bucketFromIdx], inBucketFromIdx);
		int numBucketsRight = computeNumberOfBucketsRight(shape[bucketToIdx], inBucketToIdx);
		int numBucketsCenter = bucketToIdx - bucketFromIdx - 1;

		// TODO: Wenn Anzahl buckets zu groß, dann kein Fill sondern direkt rehape (dazu
		// reshape ändern, damit reshapeSublist geht)

		FplValue[][] bucketsDst = new FplValue[numBucketsLeft + numBucketsCenter + numBucketsRight][];

		createAndFillShapeFromLeft(shape[bucketFromIdx], inBucketFromIdx, bucketsDst);
		arraycopy(shape, bucketFromIdx + 1, bucketsDst, numBucketsLeft, numBucketsCenter);
		createAndFillShapeFromRight(shape[bucketToIdx], inBucketToIdx, bucketsDst);

		return new FplList(bucketsDst);
	}

	private void createAndFillShapeFromLeft(FplValue[] bucket, int inBucketFromIdx, FplValue[][] bucketsDst) {
		if (inBucketFromIdx == 0) {
			bucketsDst[0] = bucket;
		} else {
			int bucketSize = BASE_SIZE;
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
		if (inBucketToIdx == bucket.length - 1) {
			bucketsDst[bucketsDst.length - 1] = bucket;
		} else {
			int bucketSize = BASE_SIZE;
			int bucketDstIndex = bucketsDst.length - 1;
			int rest = inBucketToIdx + 1;
			while (rest > bucketSize) {
				int size = Math.min(bucketSize / 2, rest);
				bucketsDst[bucketDstIndex] = new FplValue[size];
				arraycopy(bucket, inBucketToIdx - size, bucketsDst[bucketDstIndex], 0, size);
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
			return 1;
		}
		int count = fplValues.length - inBucketIdx;
		if (count < BASE_SIZE) {
			return 1;
		}

		return numBucketsForCount(count);
	}

	private int computeNumberOfBucketsRight(FplValue[] fplValues, int inBucketIdx) {
		if (inBucketIdx == 0) {
			return 1;
		}
		int count = inBucketIdx;
		if (count < BASE_SIZE) {
			return 1;
		}

		return numBucketsForCount(count);
	}

	private int numBucketsForCount(int count) {
		int rest = count;
		int bucketSize = BASE_SIZE;
		int buckets = 1;
		while (rest > bucketSize) {
			rest -= bucketSize / 2; // fill to half
			bucketSize *= FACTOR;
			buckets++;
		}
		return buckets;
	}

	private FplList subList(FplValue[] fplValues, int first, int last) {
		int size = last - first + 1;
		if (size <= BASE_SIZE) {
			FplValue[][] b = new FplValue[1][];
			b[0] = new FplValue[size];
			arraycopy(fplValues, first, b[0], 0, size);
			return new FplList(b);
		}
		FplValue[][] bucketsDst = createTwoSidedShape(size);
		for (int i = 0, bucketIdx = 0; bucketIdx < bucketsDst.length; bucketIdx++) {
			FplValue[] bucketDst = bucketsDst[bucketIdx];
			arraycopy(fplValues, i, bucketDst, 0, bucketDst.length);
			i += bucketDst.length;
		}
		return new FplList(bucketsDst);
	}

	/**
	 * @return Number of elements in the list.
	 */
	public int size() {
		if (linear == null) {
			return size(shape);
		} else {
			return linear.length;
		}
	}

	private FplValue[][] createTwoSidedShape(int size) {
		int numberOfBuckets = 0;
		int rest = size;
		int bucketSize = BASE_SIZE;
		while (rest > 2 * bucketSize) {
			numberOfBuckets += 2;
			rest -= 2 * (bucketSize / 2); // fill one half in each
			bucketSize *= FACTOR;
		}
		numberOfBuckets += rest > bucketSize ? 2 : 1;
		FplValue[][] createdBuckets = new FplValue[numberOfBuckets][];

		rest = size;
		bucketSize = BASE_SIZE;
		int i = 0, j = numberOfBuckets - 1;
		while (i < j - 1) {
			createdBuckets[i++] = new FplValue[bucketSize / 2];
			createdBuckets[j--] = new FplValue[bucketSize / 2];
			rest -= 2 * (bucketSize / 2);
			bucketSize *= FACTOR;
		}
		if (i == j - 1) {
			// two remaining buckets: i and i+1
			createdBuckets[i] = new FplValue[rest / 2];
			createdBuckets[j--] = new FplValue[rest - rest / 2];
		} else {
			// one remaining bucket: i = j
			createdBuckets[i] = new FplValue[rest];
		}
		return createdBuckets;
	}

	private int size(FplValue[][] b) {
		int count = 0;
		for (FplValue[] bucket : b) {
			count += bucket.length;
		}
		return count;
	}

	/**
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<FplValue> iterator() {
		if (linear == null) {
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
		} else {
			return new Iterator<FplValue>() {
				private int pos = 0;

				@Override
				public boolean hasNext() {
					return pos < linear.length;
				}

				@Override
				public FplValue next() {
					if (pos == linear.length) {
						throw new NoSuchElementException();
					}
					return linear[pos++];
				}
			};
		}
	}

	/**
	 * @see FplValue.data.LObject#evaluate(lang.data.Scope)
	 */
	@Override
	public FplValue evaluate(Scope scope) throws EvaluationException {
		if (shape.length == 0 || shape[0].length == 0) {
			return this; // empty list evaluates to empty list
		}
		FplValue firstElemUnevaluated = shape[0][0];
		FplValue firstElem = firstElemUnevaluated.evaluate(scope);
		if (!(firstElem instanceof Function)) {
			throw new EvaluationException("Not a function: " + firstElem);
		}
		Function f = (Function) firstElem;
		FplValue[] params = new FplValue[size() - 1];
		int bucketIdx = 0;
		int inBucketIdx = 0;
		for (int i = 0; i < params.length; i++) {
			inBucketIdx++;
			if (inBucketIdx == shape[bucketIdx].length) {
				inBucketIdx = 0;
				bucketIdx++;
			}
			params[i] = shape[bucketIdx][inBucketIdx];
		}
		FplValue result = f.call(scope, params);
		if (result instanceof FplFunction && firstElem instanceof PositionHolder) {
			((FplFunction) result).setPosition(((PositionHolder) firstElemUnevaluated).getPosition());
		}
		return result;
	}

	public boolean isEmpty() {
		if (linear == null) {
			return shape.length == 0 || shape[0].length == 0;
		} else {
			return linear.length == 0;
		}
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

	// Only used for testing
	int numberOfBuckets() {
		return shape.length;
	}

	private FplValue[][] reshape(FplValue[][] buckets, int size) {
		// TODO: Optimization: Don't reshape all buckets, only from left and right until
		// 1/4 (each) of elements is reached.
		int numBuckets = 2;
		int bucketSize = BASE_SIZE;
		int sizeInBuckets = bucketSize;
		while (sizeInBuckets < size) {
			bucketSize *= FACTOR;
			sizeInBuckets += bucketSize;
			numBuckets += 2;
		}
		numBuckets--;
		FplValue[][] bucketsDst = new FplValue[numBuckets][];
		bucketSize = BASE_SIZE;
		int rest = size;
		int i = 0, j = numBuckets - 1;
		while (i < j) {
			bucketsDst[i] = new FplValue[bucketSize / 2];
			bucketsDst[j] = new FplValue[bucketSize / 2];
			rest -= bucketsDst[i].length + bucketsDst[j].length;
			bucketSize *= FACTOR;
			i++;
			j--;
		}
		bucketsDst[i] = new FplValue[rest];
		int srcIdx = 0, inBucketSrcIdx = 0, dstIdx = 0, inBucketDstIdx = 0;

		// Copy entries until bucketsDst is filled completely
		while (srcIdx < buckets.length && dstIdx < bucketsDst.length) {
			int length = Math.min(buckets[srcIdx].length - inBucketSrcIdx,
					bucketsDst[dstIdx].length - inBucketDstIdx);
			arraycopy(buckets[srcIdx], inBucketSrcIdx, bucketsDst[dstIdx], inBucketDstIdx, length);
			inBucketSrcIdx += length;
			if (inBucketSrcIdx == buckets[srcIdx].length) {
				inBucketSrcIdx = 0;
				srcIdx++;
			}
			inBucketDstIdx += length;
			if (inBucketDstIdx == bucketsDst[dstIdx].length) {
				inBucketDstIdx = 0;
				dstIdx++;
			}
		}

		return bucketsDst;
	}

	private void checkNotEmpty() throws EvaluationException {
		if (isEmpty()) {
			throw new EvaluationException("List is empty");
		}
	}

	private FplValue[] bucket(FplValue element) {
		FplValue[] result = new FplValue[1];
		result[0] = element;
		return result;
	}
}
