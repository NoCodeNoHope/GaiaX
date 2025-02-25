/*
 * Copyright (c) 2021, Alibaba Group Holding Limited;
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.gaiax

import android.content.Context
import android.view.View
import com.alibaba.fastjson.JSONObject
import com.alibaba.gaiax.context.GXTemplateContext
import com.alibaba.gaiax.data.GXDataImpl
import com.alibaba.gaiax.data.assets.GXAssetsBinaryWithoutSuffixTemplate
import com.alibaba.gaiax.data.assets.GXAssetsTemplate
import com.alibaba.gaiax.data.cache.GXTemplateInfoSource
import com.alibaba.gaiax.render.GXRenderImpl
import com.alibaba.gaiax.render.node.GXNode
import com.alibaba.gaiax.render.node.GXTemplateNode
import com.alibaba.gaiax.render.node.getGXNodeById
import com.alibaba.gaiax.render.node.getGXViewById
import com.alibaba.gaiax.render.view.GXIViewBindData
import com.alibaba.gaiax.template.GXCss
import com.alibaba.gaiax.template.GXStyleConvert
import com.alibaba.gaiax.template.GXTemplateKey

/**
 * GaiaX engine class.
 *
 * GaiaX is a lightweight, pure native dynamic card cross-end solution developed by alibaba YouKu technology team.
 * In addition to the client rendering SDK, GaiaXStudio and GaiaXDemo (template samples, as well as scan preview) are provided to support the development of full-link technology support from template building and editing, real machine debugging and preview, etc.
 * GaiaX aims to ensure the performance of native experience at the same time, help the client development achieve low code.
 *
 * Basic usage:
 * ```
 * // Init GXTemplateEngine
 * GXTemplateEngine.instance.init(activity)
 * // Build template params
 * val params = GXTemplateEngine.GXTemplateItem(activity, "template biz", "template id")
 * // Build measure size
 * val size = GXTemplateEngine.GXMeasureSize(screenWidth, null)
 * // Build template data
 * val templateData = GXTemplateEngine.GXTemplateData(jsonData)
 * // Create view
 * val view = GXTemplateEngine.instance.createView(params, size)
 * // Update data
 * GXTemplateEngine.instance.bindData(view, templateData)
 * // Inject view to container
 *  findViewById<LinearLayoutCompat>(R.id.xxx).addView(view, 0)
 * ```
 */
class GXTemplateEngine {

    /**
     * GX adapter
     */
    interface GXIAdapter {
        fun init(context: Context)
    }

    /**
     * Data processing parameter
     */
    abstract class GXData {

        /**
         * View index
         */
        var index: Int? = null

        /**
         * Target view
         */
        var view: View? = null

        /**
         * Node id
         */
        var nodeId: String? = null

        /**
         * Template information
         */
        var templateItem: GXTemplateItem? = null
    }

    /**
     * Event parameters
     */
    open class GXGesture {

        /**
         * Event types：tap, longpress
         */
        var gestureType = GXTemplateKey.GAIAX_GESTURE_TYPE_TAP

        /**
         * Target view
         */
        var view: View? = null

        /**
         * Node id
         */
        var nodeId: String? = null

        /**
         * Template information
         */
        var templateItem: GXTemplateItem? = null

        /**
         * View index
         */
        var index: Int? = null

        /**
         * Event data
         */
        var eventParams: JSONObject? = null

        override fun toString(): String {
            return "GXGesture(gestureType='$gestureType', view=$view, nodeId=$nodeId, index=$index, eventParams=$eventParams)"
        }
    }

    /**
     * Track event parameters
     */
    class GXTrack {

        /**
         * Target view
         */
        var view: View? = null

        /**
         * Node id
         */
        var nodeId: String? = null

        /**
         * View index
         */
        var index: Int? = null

        /**
         * Template information
         */
        var templateItem: GXTemplateItem? = null

        /**
         * Buried data
         */
        var trackParams: JSONObject? = null

        override fun toString(): String {
            return "GXTrack(view=$view, nodeId=$nodeId, index=$index, trackParams=$trackParams)"
        }
    }

    /**
     * Text data processing parameters
     */
    class GXTextData : GXData() {

        /**
         * Text content
         */
        var text: CharSequence? = null

        /**
         * Node style
         */
        var nodeCss: GXCss? = null

        /**
         * Node data
         */
        var nodeData: JSONObject? = null

        var extendData: JSONObject? = null
    }

    /**
     * Scroll parameters
     */
    class GXScroll {
        companion object {
            const val TYPE_ON_SCROLL_STATE_CHANGED = "onScrollStateChanged"
            const val TYPE_ON_SCROLLED = "onScrolled"
        }

        var type: String = ""

        /**
         * Target view
         */
        var view: View? = null

        /**
         * Offset x
         */
        var dx: Int = 0

        /**
         * Offset y
         */
        var dy: Int = 0

        /**
         * Scroll state
         */
        var state: Int = 0

        override fun toString(): String {
            return "GXScroll(view=$view, dx=$dx, dy=$dy, state=$state)"
        }
    }

    /**
     * Animation event parameters
     */
    class GXAnimation {

        companion object {
            const val STATE_START = "START"
            const val STATE_END = "END"
        }

        /**
         * Animation state
         * START/END
         */
        var state: String? = null

        /**
         * Node id
         */
        var nodeId: String? = null

        /**
         * Lottie target view
         */
        var view: View? = null

        /**
         * Animation params
         */
        var animationParams: JSONObject? = null

        override fun toString(): String {
            return "GXAnimation(type=$state, nodeId=$nodeId, targetView=$view)"
        }
    }

    /**
     * Custom view bind data interface
     */
    interface GXICustomViewBindData : GXIViewBindData {
        override fun onBindData(data: JSONObject?) {}
    }

    /**
     * Event listener
     */
    interface GXIEventListener {

        /**
         * Gesture event
         */
        fun onGestureEvent(gxGesture: GXGesture) {}

        /**
         * Scroll event
         */
        fun onScrollEvent(gxScroll: GXScroll) {}

        /**
         * Animation event
         */
        fun onAnimationEvent(animation: GXAnimation) {}
    }

    /**
     * Track listener
     */
    interface GXITrackListener {

        /**
         * Track event
         */
        fun onTrackEvent(gxTrack: GXTrack) {}
    }

    /**
     * Data processing listeners
     */
    interface GXIDataListener {

        /**
         * Text processing event
         */
        fun onTextProcess(gxTextData: GXTextData): CharSequence? = null
    }

    /**
     * Template viewport parameters
     * Used to determine the drawable size of a template
     */
    data class GXMeasureSize(var width: Float?, var height: Float?) {

        override fun toString(): String {
            return "GXMeasureSize(width=$width, height=$height)"
        }
    }

    /**
     * Template data parameters
     */
    data class GXTemplateData(
        /**
         * Template data, used to bind data to the view
         */
        val data: JSONObject
    ) {

        var tag: Any? = null

        /**
         * @suppress
         */
        var scrollIndex: Int = -1

        /**
         * Data listener
         */
        var dataListener: GXIDataListener? = null

        /**
         * Event listener
         */
        var eventListener: GXIEventListener? = null

        /**
         * Track listener
         */
        var trackListener: GXITrackListener? = null

    }

    /**
     * Template information parameters
     */
    data class GXTemplateItem(
        /**
         * Android context
         */
        val context: Context,
        /**
         * Template biz id
         */
        var bizId: String,
        /**
         * Template id
         */
        val templateId: String
    ) {

        /**
         * Used to relocate template value paths
         * @suppress
         */
        var bundle: String = ""

        /**
         * Template version, not currently in use
         * @suppress
         */
        var templateVersion: String = ""

        /**
         * Is local template
         * @suppress
         */
        var isLocal: Boolean = false

        override fun toString(): String {
            return "GXTemplateItem(context=$context, bizId='$bizId', templateId='$templateId', templateVersion='$templateVersion'"
        }
    }

    internal lateinit var context: Context

    internal val data by lazy {
        val data = GXDataImpl(context)
        data
    }

    internal val render by lazy {
        GXRenderImpl()
    }

    fun createTemplateContext(
        gxTemplateItem: GXTemplateItem,
        gxMeasureSize: GXMeasureSize,
        gxVisualTemplateNode: GXTemplateNode?
    ): GXTemplateContext {
        val templateInfo = data.getTemplateInfo(gxTemplateItem)
        return GXTemplateContext.createContext(
            gxTemplateItem,
            gxMeasureSize,
            templateInfo,
            gxVisualTemplateNode
        )
    }

    /**
     * Create template View
     *
     * @param gxTemplateItem Template information
     * @param gxMeasureSize Measuring the Size
     *
     * @return A view generated by a template
     */
    fun createView(gxTemplateItem: GXTemplateItem, gxMeasureSize: GXMeasureSize): View {
        return createView(gxTemplateItem, gxMeasureSize, null)
    }

    /**
     * Bind template data
     *
     * @param view The root view
     * @param gxTemplateData Template data
     * @param gxMeasureSize Measure Size
     * @throws IllegalArgumentException
     */
    fun bindData(view: View, gxTemplateData: GXTemplateData, gxMeasureSize: GXMeasureSize? = null) {
        val gxTemplateContext = GXTemplateContext.getContext(view)
            ?: throw IllegalArgumentException("Not found templateContext from targetView")
        gxTemplateContext.templateData = gxTemplateData
        if (gxMeasureSize != null) {
            gxTemplateContext.size = gxMeasureSize
        }
        render.bindViewDataOnlyNodeTree(gxTemplateContext)
        render.bindViewDataOnlyViewTree(gxTemplateContext)
    }

    /**
     * @suppress
     * @hide
     */
    fun createView(
        gxTemplateItem: GXTemplateItem,
        gxMeasureSize: GXMeasureSize,
        gxVisualTemplateNode: GXTemplateNode?
    ): View {
        val templateInfo = data.getTemplateInfo(gxTemplateItem)
        val context = GXTemplateContext.createContext(
            gxTemplateItem,
            gxMeasureSize,
            templateInfo,
            gxVisualTemplateNode
        )
        return render.createView(context)
    }

    /**
     * @suppress
     * @hide
     */
    fun createViewOnlyNodeTree(
        gxTemplateItem: GXTemplateItem,
        gxMeasureSize: GXMeasureSize,
        gxVisualTemplateNode: GXTemplateNode?
    ): GXTemplateContext {
        val templateInfo = data.getTemplateInfo(gxTemplateItem)
        val context = GXTemplateContext.createContext(
            gxTemplateItem,
            gxMeasureSize,
            templateInfo,
            gxVisualTemplateNode
        )
        render.createViewOnlyNodeTree(context)
        return context
    }

    /**
     * @suppress
     * @hide
     */
    fun createViewOnlyViewTree(
        gxTemplateContext: GXTemplateContext
    ): View {
        return render.createViewOnlyViewTree(gxTemplateContext)
    }

    /**
     * @suppress
     * @hide
     */
    fun bindDataOnlyNodeTree(view: View, gxTemplateData: GXTemplateData) {
        val gxTemplateContext = GXTemplateContext.getContext(view)
            ?: throw IllegalArgumentException("Not found templateContext from targetView")
        gxTemplateContext.templateData = gxTemplateData
        render.bindViewDataOnlyNodeTree(gxTemplateContext)
    }

    /**
     * @suppress
     * @hide
     */
    fun bindDataOnlyViewTree(view: View, gxTemplateData: GXTemplateData) {
        val gxTemplateContext = GXTemplateContext.getContext(view)
            ?: throw IllegalArgumentException("Not found templateContext from targetView")
        gxTemplateContext.templateData = gxTemplateData
        render.bindViewDataOnlyViewTree(gxTemplateContext)
    }

    /**
     * Getting the template context
     * @suppress
     * @hide
     */
    fun getGXTemplateContext(targetView: View?): GXTemplateContext? {
        return targetView?.let { GXTemplateContext.getContext(it) }
    }

    /**
     * Get template View
     * @suppress
     */
    fun getGXViewById(targetView: View, id: String): View? {
        GXTemplateContext.getContext(targetView)?.let { context ->
            return context.rootNode?.getGXViewById(id)
        }
        return null
    }

    /**
     * Get GXNode
     * @suppress
     * @hide
     */
    fun getGXNodeById(targetView: View, id: String): GXNode? {
        GXTemplateContext.getContext(targetView)?.let { context ->
            return context.rootNode?.getGXNodeById(id)
        }
        return null
    }

    /**
     * SDK initialization method
     */
    fun init(context: Context): GXTemplateEngine {
        this.context = context.applicationContext
        GXStyleConvert.instance.init(context.assets)
        GXRegisterCenter.instance
            // priority 0
            .registerExtensionTemplateInfoSource(GXTemplateInfoSource(this.context), 0)
            // priority 0
            .registerExtensionTemplateSource(GXAssetsBinaryWithoutSuffixTemplate(this.context), 0)
            // priority 1
            .registerExtensionTemplateSource(GXAssetsTemplate(this.context), 1)

        // init adapter
        initGXAdapter()?.init(context)
        return this
    }

    private fun initGXAdapter(): GXIAdapter? {
        return try {
            val clazz = Class.forName("com.alibaba.gaiax.adapter.GXAdapter")
            clazz.newInstance() as GXIAdapter
        } catch (e: Exception) {
            null
        }
    }

    companion object {

        val instance by lazy {
            GXTemplateEngine()
        }
    }
}