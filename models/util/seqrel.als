module util/seqrel[elem]

/**
 * A sequence utility for modeling sequences as just a
 * relation as opposed to reifying them into sequence
 * atoms like the util/sequence module does.
 *
 * @author Greg Dennis
 */

open util/ordering[SeqIdx] as ord

sig SeqIdx {}

/* sequence covers a prefix of SeqIdx */
pred isSeq(s: SeqIdx -> elem) {
  s in (SeqIdx -> lone elem)
  s::inds[] - ord/next[s::inds[]] in ord/first[]
}

/* returns all the elements in this sequence */
fun elems (s: SeqIdx -> elem): set elem { SeqIdx.s }

/* returns the first element in the sequence */
fun first (s: SeqIdx -> elem): lone elem { s[ord/first[]] }

/* returns the last element in the sequence */
fun last (s: SeqIdx -> elem): lone elem { s[lastIdx[s]] }

/* returns the cdr of the sequence */
fun rest (s: SeqIdx -> elem) : SeqIdx -> elem {
  (ord/Ord.next_).s
}

/* returns all but the last element of the sequence */
fun butlast (s: SeqIdx -> elem) : SeqIdx -> elem {
  (SeqIdx - lastIdx[s]) <: s
}

/* true if the sequence is empty */
pred isEmpty (s: SeqIdx -> elem) { no s }

/* true if this sequence has duplicates */
pred hasDups (s: SeqIdx -> elem) { #elems[s] < #inds[s] }

/* returns all the indices occupied by this sequence */
fun inds (s: SeqIdx -> elem): set SeqIdx { s.elem }

/* returns last index occupied by this sequence */
fun lastIdx (s: SeqIdx -> elem): lone SeqIdx { ord/max[inds[s]] }

/* 
 * returns the index after the last index
 * if this sequence is empty, returns the first index,
 * if this sequence is full, returns empty set
 */
fun afterLastIdx (s: SeqIdx -> elem) : lone SeqIdx {
  ord/min[SeqIdx - inds[s]]
}

/* returns first index at which given element appears or the empty set if it doesn't */
fun idxOf (s: SeqIdx -> elem, e: elem) : lone SeqIdx { ord/min[indsOf[s, e]] }

/* returns last index at which given element appears or the empty set if it doesn't */
fun lastIdxOf (s: SeqIdx -> elem, e: elem) : lone SeqIdx { ord/max[indsOf[s, e]] }

/* returns set of indices at which given element appears or the empty set if it doesn't */
fun indsOf (s: SeqIdx -> elem, e: elem) : set SeqIdx { s.e }

/*
 * return the result of appending e to the end of s
 * just returns s if s exhausted SeqIdx
 */
fun add (s: SeqIdx -> elem, e: elem) : SeqIdx -> elem {
  setAt[s, afterLastIdx[s], e]
}

/* returns the result of setting the value at index i in sequence to e */
fun setAt (s: SeqIdx -> elem, i: SeqIdx, e: elem) : SeqIdx -> elem {
  s ++ i -> e
}

/* returns the result of inserting value e at index i */
fun insert (s: SeqIdx -> elem, i: SeqIdx, e: elem) : SeqIdx -> elem {
  (ord/prevs[i] <: s) + (i->e) + (ord/Ord.prev_).((ord/nexts[i] + i) <: s)
}

/* returns the result of deleting the value at index i */
fun delete(s: SeqIdx -> elem, i: SeqIdx) : SeqIdx -> elem {
  (ord/prevs[i] <: s) + (ord/Ord.next_).(ord/nexts[i] <: s)
}

/* appended is the result of appending s2 to s1 */
fun append (s1, s2: SeqIdx -> elem) : SeqIdx -> elem {
  let shift = {i', i: SeqIdx | #ord/prevs[i'] = #ord/prevs[i] + #ord/prevs[lastIdx[s1]] + 1 } |
    s1 + shift.s2
}

/* returns the subsequence of s between from and to, inclusive */
fun subseq (s: SeqIdx -> elem, from, to: SeqIdx) : SeqIdx -> elem {
  let shift = {i', i: SeqIdx | #ord/prevs[i'] = #ord/prevs[i] - #ord/prevs[from] } |
    shift.((SeqIdx - ord/nexts[to]) <: s)
}

//
// following operations provide all the ord operations for SeqIdx
//
fun firstIdx (): SeqIdx { ord/first[] }
fun finalIdx (): SeqIdx { ord/last [] }
fun prev (i: SeqIdx): lone SeqIdx { ord/prev[i] }
fun next (i: SeqIdx): lone SeqIdx { ord/next[i] }
fun prevs (i: SeqIdx): set SeqIdx { ord/prevs[i] }
fun nexts (i: SeqIdx): set SeqIdx { ord/nexts[i] }
pred lt (i1, i2: SeqIdx) { ord/lt[i1,i2] }
pred gt (i1, i2: SeqIdx) { ord/gt[i1,i2] }
pred lte (i1, i2: SeqIdx) { ord/lte[i1,i2] }
pred gte (i1, i2: SeqIdx) { ord/gte[i1,i2] }
fun larger  (i1, i2: SeqIdx): SeqIdx { ord/larger [i1,i2] }
fun smaller (i1, i2: SeqIdx): SeqIdx { ord/smaller[i1,i2] }
fun max (s: set SeqIdx): lone SeqIdx { ord/max[s] }
fun min (s: set SeqIdx): lone SeqIdx { ord/min[s] }
