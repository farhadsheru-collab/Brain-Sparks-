package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.GameCategory
import com.example.engine.InfiniteLevelGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Brain Sparks", appName)
  }

  @Test
  fun `verify infinite level generation for all game categories`() {
    for (category in GameCategory.values()) {
      for (level in listOf(1, 5, 12, 50, 100)) {
        val levelData = InfiniteLevelGenerator.generateLevel(category, level)
        assertNotNull("Level data should not be null for $category at level $level", levelData)
        assertTrue("Questions should be generated", levelData.questions.isNotEmpty())
        for (q in levelData.questions) {
          assertTrue("Question options should not be empty", q.options.isNotEmpty())
          assertTrue(
            "Correct index should be valid",
            q.correctIndex in 0 until q.options.size || q.visualType == com.example.engine.VisualType.MATRIX_GRID
          )
        }
      }
    }
  }
}
