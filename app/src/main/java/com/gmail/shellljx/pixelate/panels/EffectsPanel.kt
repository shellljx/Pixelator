package com.gmail.shellljx.pixelate.panels

import android.app.Activity
import android.graphics.Rect
import android.view.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.gmail.shellljx.pixelate.*
import java.util.ArrayList

class EffectsPanel(private val context: Activity) : IPanel {

    private val mEffectsRecyclerView by lazy { context.findViewById<RecyclerView>(R.id.rv_effects) }
    private val mEffectsAdapter by lazy { EffectAdapter() }
    private val effectItems = arrayListOf<EffectItem>()

    override fun onCreateView(parent: ViewGroup) {
        //do nothing
    }

    override fun onViewCreated() {
        mEffectsRecyclerView.layoutManager = GridLayoutManager(context, 5)
        mEffectsRecyclerView.adapter = mEffectsAdapter
        mEffectsRecyclerView.addItemDecoration(GridSpacingItemDecoration(5, 15, true))
    }

    fun setEffectItems(effectList: ArrayList<EffectItem>) {
        val startPosition = effectItems.size
        effectItems.addAll(effectList)
        mEffectsAdapter.notifyItemRangeInserted(startPosition, effectList.size)
    }

    inner class EffectAdapter : Adapter<ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return EffectHolder(LayoutInflater.from(context).inflate(R.layout.layout_effect_item, parent, false) as ViewGroup)
        }

        override fun getItemCount(): Int {
            return effectItems.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        }
    }

    inner class EffectHolder(root: ViewGroup) : ViewHolder(root) {

    }

    class GridSpacingItemDecoration(private val spanCount: Int, private val spacing: Int, private val includeEdge: Boolean) : RecyclerView.ItemDecoration() {

        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            val position = parent.getChildAdapterPosition(view)
            val column = position % spanCount

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount
                outRect.right = (column + 1) * spacing / spanCount

                if (position < spanCount) {
                    outRect.top = spacing
                }
                outRect.bottom = spacing
            } else {
                outRect.left = column * spacing / spanCount
                outRect.right = spacing - (column + 1) * spacing / spanCount
                if (position >= spanCount) {
                    outRect.top = spacing
                }
            }
        }
    }

}