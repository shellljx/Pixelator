package com.gmail.shellljx.pixelate

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment

/**
 * @Author: shell
 * @Email: shellljx@gmail.com
 * @Date: 2023/7/24
 * @Description:
 */
class SaveFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_save_result, container, false)
    }
}