// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.codeInsight.hints.presentation

import com.intellij.openapi.editor.event.EditorMouseEvent
import com.intellij.openapi.editor.event.EditorMouseEventArea
import com.intellij.openapi.editor.event.EditorMouseListener
import com.intellij.openapi.editor.event.EditorMouseMotionListener
import java.awt.Point

/**
 * Global mouse listeners, that provide events to inlay hints at mouse coordinates.
 */
class InlayEditorMouseListener : EditorMouseListener {
  override fun mouseClicked(e: EditorMouseEvent) {
    if (!e.isConsumed) {
      val editor = e.editor
      val event = e.mouseEvent
      if (editor.getMouseEventArea(event) != EditorMouseEventArea.EDITING_AREA) return
      val point = event.point
      val inlay = editor.inlayModel.getElementAt(point, PresentationRenderer::class.java) ?: return
      val bounds = inlay.bounds ?: return
      val inlayPoint = Point(bounds.x, bounds.y)
      val translated = Point(event.x - inlayPoint.x, event.y - inlayPoint.y)
      inlay.renderer.presentation.mouseClicked(event, translated)
    }
  }
}

class InlayEditorMouseMotionListener : EditorMouseMotionListener {
  private var activePresentation: InlayPresentation? = null

  override fun mouseMoved(e: EditorMouseEvent) {
    if (!e.isConsumed) {
      val editor = e.editor
      val event = e.mouseEvent
      // TODO here also may be handling of ESC key
      if (editor.getMouseEventArea(event) != EditorMouseEventArea.EDITING_AREA) {
        activePresentation?.mouseExited()
        return
      }
      val inlay = editor.inlayModel.getElementAt(event.point, PresentationRenderer::class.java)
      val presentation = inlay?.renderer?.presentation
      if (activePresentation != presentation) {
        activePresentation?.mouseExited()
        activePresentation = presentation
      }
      if (presentation != null) {
        val bounds = inlay.bounds ?: return
        val inlayPoint = Point(bounds.x, bounds.y)
        val translated = Point(event.x - inlayPoint.x, event.y - inlayPoint.y)
        presentation.mouseMoved(event, translated)
      }
    }
  }
}