package com.agroSystem.app.features.auth

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.agroSystem.app.data.local.AppDatabase
import kotlinx.coroutines.launch
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.agroSystem.app.R
import com.google.android.material.button.MaterialButton

class OnboardingFragment : Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var btnAction: MaterialButton
    private lateinit var btnSkip: Button
    private lateinit var layoutIndicators: LinearLayout

    private val onboardingPages = listOf(
        OnboardingPageData("Smart Tracking", "Monitor perjalanan komoditas pangan secara real-time dari lahan petani hingga ke tangan konsumen.", R.drawable.sapi),
        OnboardingPageData("Kontrol Kualitas", "Pastikan kesegaran dan standar kualitas terbaik dengan sensor pemantau suhu & kelembaban otomatis.", R.drawable.sayuran),
        OnboardingPageData("Analisis Agrobisnis", "Optimalkan rantai pasok Anda dengan wawasan analitik bertenaga AI untuk meminimalkan limbah pangan.", R.drawable.padi)
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_onboarding, container, false)
        viewPager = view.findViewById(R.id.view_pager)
        btnAction = view.findViewById(R.id.btn_action)
        btnSkip = view.findViewById(R.id.btn_skip)
        layoutIndicators = view.findViewById(R.id.layout_indicators)

        setupViewPager()
        setupIndicators()
        setCurrentIndicator(0)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setCurrentIndicator(position)
                if (position == onboardingPages.size - 1) {
                    btnAction.text = "Mulai Sekarang"
                    btnSkip.visibility = View.GONE
                } else {
                    btnAction.text = "Lanjut"
                    btnSkip.visibility = View.VISIBLE
                }
            }
        })

        btnAction.setOnClickListener {
            if (viewPager.currentItem + 1 < onboardingPages.size) {
                viewPager.currentItem = viewPager.currentItem + 1
            } else {
                navigateToPhoneInput()
            }
        }

        btnSkip.setOnClickListener {
            navigateToPhoneInput()
        }

        return view
    }

    private fun setupViewPager() {
        viewPager.adapter = OnboardingAdapter(onboardingPages)
    }

    private fun setupIndicators() {
        val layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(8, 0, 8, 0)
        }
        for (i in onboardingPages.indices) {
            val imageView = ImageView(requireContext())
            imageView.layoutParams = layoutParams
            layoutIndicators.addView(imageView)
        }
    }

    private fun setCurrentIndicator(index: Int) {
        val childCount = layoutIndicators.childCount
        for (i in 0 until childCount) {
            val imageView = layoutIndicators.getChildAt(i) as ImageView
            val width = if (i == index) 28.dpToPx() else 8.dpToPx()
            val height = 8.dpToPx()
            val drawable = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 4.dpToPx().toFloat()
                setColor(
                    ContextCompat.getColor(
                        requireContext(),
                        if (i == index) R.color.color_primary_green else R.color.color_border_grey
                    )
                )
            }
            imageView.setImageDrawable(drawable)
            imageView.layoutParams = LinearLayout.LayoutParams(width, height).apply {
                setMargins(8, 0, 8, 0)
            }
        }
    }

    private fun Int.dpToPx(): Int {
        val scale = resources.displayMetrics.density
        return (this * scale + 0.5f).toInt()
    }

    private fun navigateToPhoneInput() {
        findNavController().navigate(R.id.action_onboardingFragment_to_phoneInputFragment)
    }

    data class OnboardingPageData(val title: String, val description: String, val imageResId: Int)

    class OnboardingAdapter(private val pages: List<OnboardingPageData>) :
        RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
            return OnboardingViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_onboarding_page, parent, false)
            )
        }

        override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
            holder.bind(pages[position])
        }

        override fun getItemCount(): Int = pages.size

        class OnboardingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val image: ImageView = itemView.findViewById(R.id.image_illustration)
            private val title: TextView = itemView.findViewById(R.id.text_title)
            private val description: TextView = itemView.findViewById(R.id.text_description)

            fun bind(page: OnboardingPageData) {
                image.setImageResource(page.imageResId)
                title.text = page.title
                description.text = page.description
            }
        }
    }
}
