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
		if (data.length > 3 && (1 << data.length) > realSize) {
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
		if (buckets[0].length > BASE_SIZE + BASE_SIZE * FACTOR) {
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
		} else {
			FplValue[][] bucketsDst = new FplValue[buckets.length][];
			arraycopy(buckets, 1, bucketsDst, 1, bucketsDst.length - 1);
			bucketsDst[0] = new FplValue[buckets[0].length - 1];
			arraycopy(buckets[0], 1, bucketsDst[0], 0, bucketsDst[0].length);
			return new FplList(bucketsDst);
		}
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
		int minSize = maxSize / 2;

		// Initial phase: Add to first bucket or create new bucket (and initial carry)
		if (firstLength < maxSize) {
			bucketsDst[dstIdx] = new FplValue[firstLength + 1];
			arraycopy(buckets[bucketsSrc], 0, bucketsDst[dstIdx], 1, firstLength);
			bucketsDst[dstIdx][0] = element;
			carryLength = 0;
		} else {
			// Don't fill bucket completely, so we don't get a carry in the next add()
			bucketsDst[dstIdx] = new FplValue[minSize];
			bucketsDst[dstIdx][0] = element;
			carryBucket = buckets[bucketsSrc];
			carryStart = minSize - 1;
			carryLength = buckets[bucketsSrc].length - minSize + 1;
			arraycopy(buckets[bucketsSrc], 0, bucketsDst[dstIdx], 1, minSize - 1);
		}
		bucketsSrc++;
		dstIdx++;
		return carryToRight(buckets, bucketsSrc, bucketsDst, dstIdx, carryBucket, carryStart, carryLength, maxSize);
	}

	private FplList carryToRight(FplValue[][] bucketsSrc, int srcIdx, FplValue[][] bucketsDst, int dstIdx, //
			FplValue[] carryBucket, int carryStart, int carryLength, int maxSize) {
		int minSize;
		// Copy with potential carry
		while (carryLength != 0 || srcIdx < bucketsSrc.length) {
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
				if (carryLength > minSize) {
					bucketsDst[dstIdx] = new FplValue[minSize];
					arraycopy(carryBucket, carryStart, bucketsDst[dstIdx], 0, minSize);
					carryLength -= minSize;
					carryStart += minSize;
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
						// The following line is the reason for this complicated method: When we don't
						// have to handle any more carry, just copy *the reference* and not *the
						// content*
						// of the array: Fast and memory preserving!
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
		// No append to empty list, there is at least one bucket
		FplValue[][] bud = new FplValue[buckets.length][];
		int srcIdx = buckets.length - 1;
		int dstIdx = buckets.length - 1;
		int lastLength = buckets[srcIdx].length;
		FplValue[] carryBucket = null;
		int carryStart = 0;
		int carryLength = 0;
		int maxSize = BASE_SIZE;
		int minSize = maxSize / 2;

		// Initial phase: Add to last bucket or create new bucket (and initial carry)
		if (lastLength < maxSize) {
			bud[dstIdx] = copyOf(buckets[srcIdx], lastLength + 1);
			bud[dstIdx][lastLength] = value;
			carryLength = 0;
		} else {
			// Don't fill bucket completely, so we don't get a carry in the next add()
			bud[dstIdx] = new FplValue[minSize];
			bud[dstIdx][minSize - 1] = value;
			carryBucket = buckets[srcIdx];
			carryLength = buckets[srcIdx].length - minSize + 1;
			arraycopy(buckets[srcIdx], carryLength, bud[dstIdx], 0, minSize - 1);
		}
		srcIdx--;
		dstIdx--;
		return carryToLeft(buckets, srcIdx, bud, dstIdx, carryBucket, carryStart, carryLength, maxSize);
	}

	private static FplList carryToLeft(FplValue[][] bucketsSrc, int srcIdx, FplValue[][] bucketsDst, int dstIdx, //
			FplValue[] carryBucket, int carryStart, int carryLength, int maxSize) {
		int minSize;
		// Copy with potential carry
		while (carryLength != 0 || srcIdx >= 0) {
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
				if (carryLength > minSize) {
					bucketsDst[dstIdx] = new FplValue[minSize];
					arraycopy(carryBucket, carryStart + carryLength - minSize, bucketsDst[dstIdx], 0, minSize);
					carryLength -= minSize;
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
						// The following line is the reason for this complicated method: When we don't
						// have to handle any more carry, just copy *the reference* and not *the
						// content*
						// of the array: Fast and memory preserving!
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
	 * @return Number of elements in the list.
	 */
	public int size() {
		return size(buckets);
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

			@Override
			public boolean hasNext() {
				return bucketsIdx < buckets.length && inBucketIdx < buckets[bucketsIdx].length;
			}

			@Override
			public FplValue next() {
				FplValue result;
				try {
					result = buckets[bucketsIdx][inBucketIdx];
					inBucketIdx++;
					if (inBucketIdx == buckets[bucketsIdx].length) {
						bucketsIdx++;
						inBucketIdx = 0;
					}
				} catch (IndexOutOfBoundsException e) {
					throw new NoSuchElementException();
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
			bucketsDst[i++] = new FplValue[bucketSize / 2];
			bucketsDst[j--] = new FplValue[bucketSize / 2];
			rest -= bucketSize;
			bucketSize *= FACTOR;
		}
		bucketsDst[i] = new FplValue[rest];
		int srcIdx = 0, inBucketSrcIdx = 0, dstIdx = 0, inBucketDstIdx = 0;

		// Copy entries until bucketsDst is filled completely
		while (srcIdx < bucketsSrc.length) {
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
		if (buckets.length == 0 || buckets[0].length == 0) {
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
