package com.desarrollodroide.pagekeeper.ui.feed

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Which way the feed just moved, which is what decides whether the scroll-to-top button is offered.
 *
 * It used to be offered on the way down too, for the whole descent. In the two pane layout the list
 * column is 340dp and the add button already sits in that corner, so the pair covered about a third
 * of a card while the reader was going forwards and had asked for nothing.
 */
class ScrollDirectionTest {

    @Test
    fun `moving into an earlier item is upwards`() {
        assertTrue(movedTowardsTop(index = 3, offset = 40, previousIndex = 4, previousOffset = 10))
    }

    @Test
    fun `moving into a later item is downwards`() {
        assertFalse(movedTowardsTop(index = 5, offset = 10, previousIndex = 4, previousOffset = 900))
    }

    /**
     * The whole reason the index is checked first. Crossing into the next item resets the offset to
     * near zero, so a smaller offset than last time is the normal look of scrolling *down* past a
     * boundary. Reading the offset alone would call this upwards.
     */
    @Test
    fun `a smaller offset in a later item is still downwards`() {
        assertFalse(movedTowardsTop(index = 5, offset = 8, previousIndex = 4, previousOffset = 940))
    }

    /** Most scrolling never changes the index: a tall card can be a whole screen of movement. */
    @Test
    fun `moving up inside one item is upwards`() {
        assertTrue(movedTowardsTop(index = 4, offset = 120, previousIndex = 4, previousOffset = 400))
    }

    @Test
    fun `moving down inside one item is downwards`() {
        assertFalse(movedTowardsTop(index = 4, offset = 400, previousIndex = 4, previousOffset = 120))
    }

    /**
     * Standing still is not moving up. The caller holds the previous answer while the grid is at
     * rest, so a button that was showing keeps showing and can be tapped; this only says that a
     * fresh reading identical to the last one is no reason to start showing it.
     */
    @Test
    fun `standing still is not upwards`() {
        assertFalse(movedTowardsTop(index = 4, offset = 120, previousIndex = 4, previousOffset = 120))
    }
}
