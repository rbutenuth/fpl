package de.codecentric.fpl.datatypes;

import java.util.ArrayList;
import static java.util.Arrays.copyOf;
import static java.lang.System.arraycopy;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.data.PositionHolder;
import de.codecentric.fpl.data.Scope;

/**
 * An FPL List.
 */
public class FplList implements FplValue, Iterable<FplValue> {
	public static final FplList EMPTY_LIST = new FplList();

	private static final int BASE_SIZE = 8;
	private static final int FACTOR = 4;

	private final FplValue[][] buckets;

	// private because there is EMPTY_LIST
	private FplList() {
		buckets = new FplValue[0][];
	}

	// Without reshape
	private FplList(FplValue[][] data) {
		this.buckets = data;
	}

	// With reshape, size < 0 means "unknown"
	private FplList(FplValue[][] data, int size) {
		int realSize = size >= 0 ? size : size(data);
		if (data.length > 3 && (1 << data.length) > realSize + 1) {
			buckets = reshape(data, realSize);
		} else {
			buckets = data;
		}
	}

	/**
	 * Create a list from one value
	 * 
	 * @param value
	 *            The value
	 */
	public FplList(FplValue value) {
		buckets = new FplValue[1][];
		buckets[0] = bucket(value);
	}

	/**
	 * Create a list.
	 * 
	 * @param values
	 *            Array with values, the values will NOT be copied, so don't modify
	 *            the array.
	 */
	public FplList(FplValue[] values) {
		buckets = new FplValue[1][];
		buckets[0] = values;
	}

	public FplList(Iterator<FplValue> iter) {
		buckets = new FplValue[1][];
		ArrayList<FplValue> li = new ArrayList<>();
		while (iter.hasNext()) {
			li.add(iter.next());
		}
		buckets[0] = li.toArray(new FplValue[li.size()]);
	}

	public FplList(List<FplValue> values) {
		buckets = new FplValue[1][];
		buckets[0] = values.toArray(new FplValue[values.size()]);
	}

	/**
	 * @return First element of the list.
	 * @throws EvaluationException
	 *             If list is empty.
	 */
	public FplValue first() throws EvaluationException {
		checkNotEmpty();
		return buckets[0][0];
	}

	/**
	 * @return Last element of the list.
	 * @throws EvaluationException
	 *             If list is empty.
	 */
	public FplValue last() throws EvaluationException {
		checkNotEmpty();
		FplValue[] lastBucket = buckets[buckets.length - 1];
		return lastBucket[lastBucket.length - 1];
	}

	/**
	 * @return Sublist without the first element.
	 * @throws EvaluationException
	 *             If list is empty.
	 */
	public FplList removeFirst() throws EvaluationException {
		checkNotEmpty();
		if (buckets.length == 1 && buckets[0].length == 1) {
			return EMPTY_LIST;
		}
		if (buckets[0].length == 1) {
			FplValue[][] bucketsDst = new FplValue[buckets.length - 1][];
			arraycopy(buckets, 1, bucketsDst, 0, bucketsDst.length);
			return new FplList(bucketsDst);
		}
		if (buckets[0].length <= BASE_SIZE) {
			FplValue[][] bucketsDst = new FplValue[buckets.length][];
			arraycopy(buckets, 1, bucketsDst, 1, bucketsDst.length - 1);
			bucketsDst[0] = new FplValue[buckets[0].length - 1];
			arraycopy(buckets[0], 1, bucketsDst[0], 0, bucketsDst[0].length);
			return new FplList(bucketsDst);
		}
		// First bucket is too large, split it according "ideal" shape
		int count = buckets[0].length - 1;
		int additionalBuckets = -1;
		int bucketFillSize = BASE_SIZE / 2;
		while (count > 0) {
			additionalBuckets++;
			count -= bucketFillSize;
			bucketFillSize *= FACTOR;
		}

		FplValue[][] bucketsDst = new FplValue[buckets.length + additionalBuckets][];
		bucketFillSize = BASE_SIZE / 2;
		bucketsDst[0] = new FplValue[bucketFillSize];
		arraycopy(buckets[0], 1, bucketsDst[0], 0, bucketFillSize);

		int dstIdx = 1;
		count = buckets[0].length - 1 - bucketFillSize;
		int inBucketIdx = bucketFillSize + 1;
		while (count > 0) {
			bucketFillSize *= FACTOR;
			if (bucketFillSize > count) {
				bucketFillSize = count;
			}
			bucketsDst[dstIdx] = new FplValue[bucketFillSize];
			arraycopy(buckets[0], inBucketIdx, bucketsDst[dstIdx], 0, bucketFillSize);
			dstIdx++;
			inBucketIdx += bucketFillSize;
			count -= bucketFillSize;
		}
		int srcIdx = 1;
		while (dstIdx < bucketsDst.length) {
			bucketsDst[dstIdx++] = buckets[srcIdx++];
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
		if (buckets.length == 1 && buckets[0].length == 1) {
			return EMPTY_LIST;
		}
		int lastIdx = buckets.length - 1;
		if (buckets[lastIdx].length == 1) {
			FplValue[][] bucketsDst = new FplValue[buckets.length - 1][];
			arraycopy(buckets, 0, bucketsDst, 0, bucketsDst.length);
			return new FplList(bucketsDst);
		}
		if (buckets[lastIdx].length <= BASE_SIZE + BASE_SIZE * FACTOR) {
			FplValue[][] bucketsDst = new FplValue[buckets.length][];
			arraycopy(buckets, 0, bucketsDst, 0, bucketsDst.length - 1);
			bucketsDst[lastIdx] = new FplValue[buckets[lastIdx].length - 1];
			arraycopy(buckets[0], 0, bucketsDst[0], 0, bucketsDst[lastIdx].length);
			return new FplList(bucketsDst);
		}
		int count = buckets[lastIdx].length - 1;
		int additionalBuckets = -1;
		int bucketFillSize = BASE_SIZE / 2;
		while (count > 0) {
			additionalBuckets++;
			count -= bucketFillSize;
			bucketFillSize *= FACTOR;
		}

		FplValue[][] bucketsDst = new FplValue[buckets.length + additionalBuckets][];
		bucketFillSize = BASE_SIZE / 2;
		int dstIdx = buckets.length + additionalBuckets - 1;
		int inBucketIdx = buckets[lastIdx].length - bucketFillSize - 1;
		bucketsDst[dstIdx] = new FplValue[bucketFillSize];
		arraycopy(buckets[lastIdx], inBucketIdx, bucketsDst[dstIdx], 0, bucketFillSize);

		dstIdx--;
		count = buckets[lastIdx].length - 1 - bucketFillSize;
		while (count > 0) {
			bucketFillSize *= FACTOR;
			if (bucketFillSize > count) {
				bucketFillSize = count;
			}
			inBucketIdx -= bucketFillSize;
			bucketsDst[dstIdx] = new FplValue[bucketFillSize];
			arraycopy(buckets[lastIdx], inBucketIdx, bucketsDst[dstIdx], 0, bucketFillSize);
			dstIdx--;
			count -= bucketFillSize;
		}
		lastIdx--;
		while (dstIdx >= 0) {
			bucketsDst[dstIdx--] = buckets[lastIdx--];
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
		if (position < 0) {
			throw new EvaluationException("position < 0");
		}
		int bucketIdx = 0;
		int count = 0;
		while (count + buckets[bucketIdx].length <= position) {
			count += buckets[bucketIdx].length;
			bucketIdx++;
			if (bucketIdx >= buckets.length) {
				throw new EvaluationException("position >= size");
			}
		}
		return buckets[bucketIdx][position - count];
	}

	/**
	 * @param element
	 *            Element to insert at front.
	 * @return New List: This list plus one new element at front.
	 */
	public FplList addAtStart(FplValue element) {
		// Shortcut for cons to empty list
		if (buckets.length == 0) {
			return new FplList(element);
		}
		// No append to empty list, there is at least one bucket
		FplValue[][] bucketsDst = new FplValue[buckets.length][];
		int bucketsSrc = 0;
		int dstIdx = 0;
		int firstLength = buckets[bucketsSrc].length;
		FplValue[] carryBucket = null;
		int carryStart = 0;
		int carryLength = 0;
		int maxSize = BASE_SIZE;

		// Initial phase: Add to first bucket or create new bucket (and initial carry)
		if (firstLength < maxSize) {
			bucketsDst[dstIdx] = new FplValue[firstLength + 1];
			arraycopy(buckets[bucketsSrc], 0, bucketsDst[dstIdx], 1, firstLength);
			bucketsDst[dstIdx][0] = element;
			carryLength = 0;
		} else {
			bucketsDst[dstIdx] = new FplValue[1];
			bucketsDst[dstIdx][0] = element;
			carryBucket = buckets[bucketsSrc];
			carryLength = buckets[bucketsSrc].length;
		}
		bucketsSrc++;
		dstIdx++;
		return carryToRight(buckets, bucketsSrc, bucketsDst, dstIdx, carryBucket, carryStart, carryLength, 2 * maxSize);
	}

	private FplList carryToRight(FplValue[][] bucketsSrc, int srcIdx, FplValue[][] bucketsDst, int dstIdx, //
			FplValue[] carryBucket, int carryStart, int carryLength, int maxSize) {
		int minSize;
		// Copy with potential carry
		while (carryLength != 0) {
			maxSize *= FACTOR;
			minSize = maxSize / 2;
			while (carryLength >= maxSize) {
				if (dstIdx == bucketsDst.length) {
					bucketsDst = copyOf(bucketsDst, bucketsDst.length + 1);
				}
				// Again: Don't fill completely, only to minSize
				bucketsDst[dstIdx] = new FplValue[minSize];
				arraycopy(carryBucket, carryStart, bucketsDst[dstIdx], 0, minSize);
				carryLength -= minSize;
				carryStart += minSize;
				dstIdx++;
				maxSize *= FACTOR;
				minSize = maxSize / 2;
			}
			// now: carryLength < maxSize
			if (dstIdx == bucketsDst.length) {
				bucketsDst = copyOf(bucketsDst, bucketsDst.length + 1);
			}
			int sourceLength = srcIdx < bucketsSrc.length ? bucketsSrc[srcIdx].length : 0;
			if (carryLength + sourceLength >= maxSize) {
				// The second part (sourceLengeh > maxSize) avoids the splitting of a large
				// block.
				// Results in one more bucket, but avoids copying values around.
				if (carryLength > minSize || sourceLength > maxSize) {
					int copyLength = Math.min(carryLength, minSize);
					bucketsDst[dstIdx] = new FplValue[copyLength];
					arraycopy(carryBucket, carryStart, bucketsDst[dstIdx], 0, copyLength);
					carryLength -= copyLength;
					carryStart += copyLength;
					dstIdx++;
				} else {
					// carry fits completely, source bucket not
					bucketsDst[dstIdx] = new FplValue[minSize];
					int takeFromBucket = minSize - carryLength;
					arraycopy(carryBucket, carryStart, bucketsDst[dstIdx], 0, carryLength);
					arraycopy(bucketsSrc[srcIdx], 0, bucketsDst[dstIdx], carryLength, takeFromBucket);
					carryLength = bucketsSrc[srcIdx].length - takeFromBucket;
					carryBucket = bucketsSrc[srcIdx];
					carryStart = takeFromBucket;
					srcIdx++;
					dstIdx++;
				}
			} else { // carry + next bucket fit in destination bucket
				if (srcIdx < bucketsSrc.length) {
					if (carryLength > 0) {
						bucketsDst[dstIdx] = new FplValue[carryLength + sourceLength];
						arraycopy(carryBucket, carryStart, bucketsDst[dstIdx], 0, carryLength);
						arraycopy(bucketsSrc[srcIdx], 0, bucketsDst[dstIdx], carryLength, sourceLength);
						carryLength = 0;
					} else {
						bucketsDst[dstIdx] = bucketsSrc[srcIdx];
					}
				} else {
					bucketsDst[dstIdx] = new FplValue[carryLength];
					arraycopy(carryBucket, carryStart, bucketsDst[dstIdx], 0, carryLength);
					carryLength = 0;
				}
				srcIdx++;
				dstIdx++;
			}
		}
		while (srcIdx < bucketsSrc.length) {
			if (dstIdx == bucketsDst.length) {
				bucketsDst = copyOf(bucketsDst, bucketsDst.length + 1);
			}
			bucketsDst[dstIdx++] = bucketsSrc[srcIdx++];
		}

		return new FplList(bucketsDst);
	}

	/**
	 * Append one element to a list.
	 * 
	 * @param value
	 *            Element to be appended
	 * @return New List: This list plus the new element at the end.
	 */
	public FplList addAtEnd(FplValue value) {
		// Shortcut for append to empty list
		if (buckets.length == 0) {
			return new FplList(value);
		}
		// Not empty, there is at least one bucket
		FplValue[][] bud = new FplValue[buckets.length][];
		int srcIdx = buckets.length - 1;
		int dstIdx = buckets.length - 1;
		int lastLength = buckets[srcIdx].length;
		FplValue[] carryBucket = null;
		int carryStart = 0;
		int carryLength = 0;
		int maxSize = BASE_SIZE;

		// Initial phase: Add to last bucket or create new bucket (and initial carry)
		if (lastLength < maxSize) {
			bud[dstIdx] = copyOf(buckets[srcIdx], lastLength + 1);
			bud[dstIdx][lastLength] = value;
			carryLength = 0;
		} else {
			bud[dstIdx] = new FplValue[1];
			bud[dstIdx][0] = value;
			carryBucket = buckets[srcIdx];
			carryLength = buckets[srcIdx].length;
		}
		srcIdx--;
		dstIdx--;
		return carryToLeft(buckets, srcIdx, bud, dstIdx, carryBucket, carryStart, carryLength, 2 * maxSize);
	}

	private static FplList carryToLeft(FplValue[][] bucketsSrc, int srcIdx, FplValue[][] bucketsDst, int dstIdx, //
			FplValue[] carryBucket, int carryStart, int carryLength, int maxSize) {
		int minSize;
		// Copy with potential carry
		while (carryLength != 0) {
			maxSize *= FACTOR;
			minSize = maxSize / 2;
			while (carryLength >= maxSize) {
				if (dstIdx < 0) {
					bucketsDst = incrementSizeLeft(bucketsDst);
					dstIdx = 0;
				}
				// Again: Don't fill completely, only to minSize
				bucketsDst[dstIdx] = new FplValue[minSize];
				arraycopy(carryBucket, carryStart + carryLength - minSize, bucketsDst[dstIdx], 0, minSize);
				carryLength -= minSize;
				dstIdx--;
				maxSize *= FACTOR;
				minSize = maxSize / 2;
			}
			// now: carryLength < maxSize
			if (dstIdx < 0) {
				bucketsDst = incrementSizeLeft(bucketsDst);
				dstIdx = 0;
			}
			int sourceLength = srcIdx >= 0 ? bucketsSrc[srcIdx].length : 0;
			if (carryLength + sourceLength >= maxSize) {
				// The second part (sourceLengeh > maxSize) avoids the splitting of a large
				// block.
				// Results in one more bucket, but avoids copying values around.
				if (carryLength > minSize || sourceLength > maxSize) {
					int copyLength = Math.min(carryLength, minSize);
					bucketsDst[dstIdx] = new FplValue[copyLength];
					arraycopy(carryBucket, carryStart + carryLength - copyLength, bucketsDst[dstIdx], 0, copyLength);
					carryLength -= copyLength;
					dstIdx--;
				} else {
					// carry fits completely, source bucket not
					bucketsDst[dstIdx] = new FplValue[minSize];
					int takeFromBucket = minSize - carryLength;
					arraycopy(bucketsSrc[srcIdx], bucketsSrc[srcIdx].length - takeFromBucket, bucketsDst[dstIdx], 0,
							takeFromBucket);
					arraycopy(carryBucket, carryStart, bucketsDst[dstIdx], takeFromBucket, carryLength);
					carryLength = bucketsSrc[srcIdx].length - takeFromBucket;
					carryBucket = bucketsSrc[srcIdx];
					carryStart = 0;
					srcIdx--;
					dstIdx--;
				}
			} else { // carry + next bucket fit in destination bucket
				if (srcIdx >= 0) {
					if (carryLength > 0) {
						bucketsDst[dstIdx] = new FplValue[carryLength + sourceLength];
						arraycopy(bucketsSrc[srcIdx], 0, bucketsDst[dstIdx], 0, sourceLength);
						arraycopy(carryBucket, carryStart, bucketsDst[dstIdx], sourceLength, carryLength);
						carryLength = 0;
					} else {
						bucketsDst[dstIdx] = bucketsSrc[srcIdx];
					}
				} else {
					bucketsDst[dstIdx] = new FplValue[carryLength];
					arraycopy(carryBucket, carryStart, bucketsDst[dstIdx], 0, carryLength);
					carryLength = 0;
				}
				srcIdx--;
				dstIdx--;
			}
		}
		while (srcIdx >= 0) {
			if (dstIdx < 0) {
				bucketsDst = incrementSizeLeft(bucketsDst);
				dstIdx = 0;
			}
			bucketsDst[dstIdx--] = bucketsSrc[srcIdx--];
		}

		return new FplList(bucketsDst);
	}

	/**
	 * Append a second list to this list.
	 * 
	 * @param list
	 *            List to append.
	 * @return This list with appended list.
	 */
	public FplList append(FplList list) {
		if (buckets.length == 0) {
			return list;
		}
		if (list == null || list.buckets.length == 0) {
			return this;
		} else {
			FplValue[][] b = new FplValue[buckets.length + list.buckets.length][];
			arraycopy(buckets, 0, b, 0, buckets.length);
			arraycopy(list.buckets, 0, b, buckets.length, list.buckets.length);
			return new FplList(b, -1);
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
		int bucketFromIdx = 0;
		int count = 0;
		int lastCount = 0;
		while (count + buckets[bucketFromIdx].length <= fromIndex) {
			lastCount = count;
			count += buckets[bucketFromIdx].length;
			bucketFromIdx++;
			if (bucketFromIdx >= buckets.length) {
				throw new EvaluationException("fromIndex >= size");
			}
		}
		int inBucketFromIdx = fromIndex - count;

		int bucketToIdx = bucketFromIdx;
		count = lastCount;
		while (count + buckets[bucketToIdx].length <= toIndex - 1) {
			count += buckets[bucketToIdx].length;
			bucketToIdx++;
			if (bucketToIdx >= buckets.length) {
				throw new EvaluationException("toIndex >= size + 1");
			}
		}
		int inBucketToIdx = toIndex - 1 - count;

		if (fromIndex == 0 && bucketToIdx == buckets.length - 1 && inBucketToIdx == buckets[bucketToIdx].length - 1) {
			return this;
		}
		
		if (bucketFromIdx == bucketToIdx) {
			return subList(buckets[bucketFromIdx], inBucketFromIdx, inBucketToIdx);
		}
		int numBucketsLeft = computeNumberOfBucketsLeft(buckets[bucketFromIdx], inBucketFromIdx);
		int numBucketsRight = computeNumberOfBucketsRight(buckets[bucketToIdx], inBucketToIdx);
		int numBucketsCenter = bucketToIdx - bucketFromIdx - 1;

		// TODO: Wenn Anzahl buckets zu groß, dann kein Fill sondern direkt rehape (dazu reshape ändern, damit reshapeSublist geht)
		
		FplValue[][] bucketsDst = new FplValue[numBucketsLeft + numBucketsCenter + numBucketsRight][];

		createAndFillShapeFromLeft(buckets[bucketFromIdx], inBucketFromIdx, bucketsDst);
		arraycopy(buckets, bucketFromIdx + 1, bucketsDst, numBucketsLeft, numBucketsCenter);
		createAndFillShapeFromRight(buckets[bucketToIdx], inBucketToIdx, bucketsDst);
		
		return new FplList(bucketsDst);
	}

	private void createAndFillShapeFromLeft(FplValue[] bucket, int inBucketFromIdx, FplValue[][] bucketsDst) {
		if (inBucketFromIdx == 0 ) {
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
			rest -= bucketSize / 2;  // fill to half
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
		return size(buckets);
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
				FplValue result = buckets[bucketsIdx][inBucketIdx];
					inBucketIdx++;
					if (inBucketIdx == buckets[bucketsIdx].length) {
						bucketsIdx++;
						inBucketIdx = 0;
						atEnd = bucketsIdx == buckets.length;
					}
				return result;
			}
		};
	}

	/**
	 * @see FplValue.data.LObject#evaluate(lang.data.Scope)
	 */
	@Override
	public FplValue evaluate(Scope scope) throws EvaluationException {
		if (buckets.length == 0 || buckets[0].length == 0) {
			return this; // empty list evaluates to empty list
		}
		FplValue firstElemUnevaluated = buckets[0][0];
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
			if (inBucketIdx == buckets[bucketIdx].length) {
				inBucketIdx = 0;
				bucketIdx++;
			}
			params[i] = buckets[bucketIdx][inBucketIdx];
		}
		FplValue result = f.call(scope, params);
		if (result instanceof FplFunction && firstElem instanceof PositionHolder) {
			((FplFunction) result).setPosition(((PositionHolder) firstElemUnevaluated).getPosition());
		}
		return result;
	}

	public boolean isEmpty() {
		return buckets.length == 0 || buckets[0].length == 0;
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
		return buckets.length;
	}

	private FplValue[][] reshape(FplValue[][] bucketsSrc, int size) {
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
		while (srcIdx < bucketsSrc.length && dstIdx < bucketsDst.length) {
			int length = Math.min(bucketsSrc[srcIdx].length - inBucketSrcIdx,
					bucketsDst[dstIdx].length - inBucketDstIdx);
			arraycopy(bucketsSrc[srcIdx], inBucketSrcIdx, bucketsDst[dstIdx], inBucketDstIdx, length);
			inBucketSrcIdx += length;
			if (inBucketSrcIdx == bucketsSrc[srcIdx].length) {
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

	private static FplValue[][] incrementSizeLeft(FplValue[][] d) {
		FplValue[][] newD = new FplValue[d.length + 1][];
		arraycopy(d, 0, newD, 1, d.length);
		return newD;
	}

	private FplValue[] bucket(FplValue element) {
		FplValue[] result = new FplValue[1];
		result[0] = element;
		return result;
	}
}
