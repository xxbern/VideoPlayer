package com.tainzhi.android.videoplayer.ui.douyu

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.tainzhi.android.videoplayer.R
import com.tainzhi.android.common.base.ui.BaseBindingFragment
import com.tainzhi.android.videoplayer.databinding.DouyuFragmentBinding

/**
 * @author:      tainzhi
 * @mail:        qfq61@qq.com
 * @date:        2020/6/15 09:32
 * @description: 斗鱼首页
 **/

class DouyuFragment : BaseBindingFragment<DouyuFragmentBinding>() {
    private val defaultChannels = arrayListOf(
            ChannelIdRoom(-1, "推荐"),
            ChannelIdRoom(3, "DOTA2"),
            ChannelIdRoom(1, "LOL"),
            ChannelIdRoom(270, "大逃杀"),
            ChannelIdRoom(181, "王者荣耀"),
            ChannelIdRoom(6, "CS:GO")
    )

    private val onPageChangeCallback by lazy {
        object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {

            }
        }
    }

    override fun getLayoutResId() = R.layout.douyu_fragment

    override fun initView() {
        mBinding.douyuViewPager.run {
            offscreenPageLimit = 4
            adapter = object : FragmentStateAdapter(this@DouyuFragment) {
                override fun createFragment(position: Int): Fragment {
                    return DouyuGameFragment.newInstance(defaultChannels[position].id.toString())
                }

                override fun getItemCount() = defaultChannels.size
            }
        }
        TabLayoutMediator(mBinding.douyuTabLayout, mBinding.douyuViewPager) { tab, position ->
            tab.text = defaultChannels[position].name
        }.attach()

        mBinding.douyuMore.setOnClickListener { view ->
            view.findNavController().navigate(R.id.action_douyuFragment_to_douyuCategoryFragment)
        }
    }

    override fun initData() {

    }

}

data class ChannelIdRoom(val id: Int, val name: String)
