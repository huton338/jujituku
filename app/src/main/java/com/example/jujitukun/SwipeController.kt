package com.example.jujitukun

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView


//参照
//https://codeburst.io/android-swipe-menu-with-recyclerview-8f28a235ff28
//https://github.com/FanFataL/swipe-controller-demo/blob/master/app/src/main/java/pl/fanfatal/swipecontrollerdemo/SwipeController.java#L65

class SwipeController(buttonActions: SwipeControllerActions) : ItemTouchHelper.Callback() {

    //コンストラクタ
    var buttonActions: SwipeControllerActions? = null

    init {
        this.buttonActions = buttonActions
    }

    private var swipeBack: Boolean = false
    private var buttonShowedState = ButtonsState.GONE
    private val buttonWidth: Float = 200F
    private var currentItemViewHolder: RecyclerView.ViewHolder? = null
    private var buttonInstance: RectF? = null

    //マージンを加えないとcardviewのマージンと合わなくなる
    private val paddingtopBottom = 10F
    private val paddingSide = 20F


    //可動域の設定（上下,左右）
    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        return makeMovementFlags(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        )
    }

    //viewを左右に動かした時に発火
    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
    }

    //viewを上下に動かした時発火
    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }


    //TODO:この関数の必要性よく分からない
    //swipe位置を返す
    override fun convertToAbsoluteDirection(flags: Int, layoutDirection: Int): Int {
        //swipeを止めた時点でif分岐に入る
        if (swipeBack) {
            //swipeした位置がボタンの長さより 短い（false）,長い(true)
            swipeBack = buttonShowedState != ButtonsState.GONE
            //swipeしすぎてViewHolderが画面外に行かないように位置を初期化(動かす前)
            return 0
        }
        return super.convertToAbsoluteDirection(flags, layoutDirection)
    }


    //viewを動かした時の見た目の設定
    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            if (buttonShowedState != ButtonsState.GONE) {
                //ボタン分の空白がある位置を初期位置にする分岐
                var dXAssgin: Float = dX
                //swipe後のボタンの余白を作成
                if (buttonShowedState == ButtonsState.LEFT_VISIBLE) dXAssgin =
                    Math.max(dX, buttonWidth)
                if (buttonShowedState == ButtonsState.RIGHT_VISIBLE) dXAssgin =
                    Math.min(dX, -buttonWidth)
                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dXAssgin,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            } else {
                //空白なしの位置を初期位置にする分岐
                setTouchListener(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }
        }
        if (buttonShowedState == ButtonsState.GONE) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
        currentItemViewHolder = viewHolder
    }

    fun onDraw(c: Canvas): Unit {
        currentItemViewHolder?.let { drawButtons(c, it) }
    }


    //dxは左方向にスワイプしている場合マイナスの値
    //dYは下方向にスワイプしている場合マイナスの値
    private fun setTouchListener(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        recyclerView.setOnTouchListener { v, event ->
            //UP+DOWNの同時発生(＝キャンセル)の場合またはタッチした状態をやめた場合（UPのみ）
            swipeBack =
                (event.action == MotionEvent.ACTION_CANCEL) or (event.action == MotionEvent.ACTION_UP)
            if (swipeBack) {
                if (dX < -buttonWidth) {
                    buttonShowedState = ButtonsState.RIGHT_VISIBLE
                } else if (dX > buttonWidth) {
                    buttonShowedState = ButtonsState.LEFT_VISIBLE
                }
                //swipeしてボタンが見える範囲まで到達している場合
                if (buttonShowedState != ButtonsState.GONE) {
                    setTouchDownListener(
                        c,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )
                    setItemsClickable(recyclerView, false)
                }
            }
            false
        }

    }

    /**
     * MotionEvent.ACTION_DOWNが発生した時にのみの動作を行うリスナー.
     */
    private fun setTouchDownListener(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {


        recyclerView.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) { //タッチ押下時
                setTouchUpListener(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }
            false
        }
    }

    /**
     * MotionEvent.ACTION_UPが発生した時にのみの動作を行うリスナー.
     */
    private fun setTouchUpListener(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {


        recyclerView.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) { //タッチやめた時
                //ここまで到達するのはMotionEventがDown→UPの場合のみ
                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    0F,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
                recyclerView.setOnTouchListener { v, event -> false }
                setItemsClickable(recyclerView, true)
                swipeBack = false

                //butonActionとbuttonのエベントと位置が取得できた場合
                //containsがnullの場合はfalse
                if (buttonActions != null && buttonInstance?.contains(
                        event.getX(),
                        event.getY()
                    ) ?: false
                ) {
                    if (buttonShowedState == ButtonsState.LEFT_VISIBLE) {
                        buttonActions?.onLeftClicked(viewHolder.getAdapterPosition())
                    } else if (buttonShowedState == ButtonsState.RIGHT_VISIBLE) {
                        buttonActions?.onRightClicked(viewHolder.getAdapterPosition())
                    }
                }
                buttonShowedState = ButtonsState.GONE
                currentItemViewHolder = null
            }
            false
        }
    }

    //swipe中のクリック防止
    private fun setItemsClickable(
        recyclerView: RecyclerView,
        isClickable: Boolean
    ) {
        for (i in 0 until recyclerView.childCount) {
            recyclerView.getChildAt(i).isClickable = isClickable
        }
    }


    enum class ButtonsState {
        GONE,
        LEFT_VISIBLE,
        RIGHT_VISIBLE
    }


    private fun drawButtons(c: Canvas, viewHolder: RecyclerView.ViewHolder): Unit {
        val buttonWidthWithoutPadding: Float = buttonWidth - 20F
        val corners: Float = 16F

        var itemView = viewHolder.itemView
        var p = Paint()

        //四角形を描画
        val leftButton = RectF(
            itemView.left.toFloat() + paddingSide,
            itemView.top.toFloat() + paddingtopBottom,
            itemView.left.toFloat() + buttonWidthWithoutPadding,
            itemView.bottom.toFloat() - paddingtopBottom
        )
        p.setColor(Color.BLUE)
        c.drawRoundRect(leftButton, corners, corners, p)
        drawText("完了", c, leftButton, p)

        val rightButton = RectF(
            itemView.right.toFloat() - buttonWidthWithoutPadding,
            itemView.top.toFloat() + paddingtopBottom,
            itemView.right.toFloat() - paddingSide,
            itemView.bottom.toFloat() - paddingtopBottom
        )
        p.setColor(Color.RED)
        c.drawRoundRect(rightButton, corners, corners, p)
        drawText("削除", c, rightButton, p)

        buttonInstance = null
        if (buttonShowedState == ButtonsState.LEFT_VISIBLE) {
            buttonInstance = leftButton
        } else if (buttonShowedState == ButtonsState.RIGHT_VISIBLE) {
            buttonInstance = rightButton
        }
    }

    private fun drawText(text: String, c: Canvas, button: RectF, p: Paint) {
        val textSize = 30F
        p.setColor(Color.WHITE)
        p.setAntiAlias(true)
        p.textSize = textSize

        val textWidth = p.measureText(text)
        c.drawText(
            text,
            button.centerX() - (textWidth / 2),
            button.centerY() + (textSize / 2),
            p
        )
    }


}