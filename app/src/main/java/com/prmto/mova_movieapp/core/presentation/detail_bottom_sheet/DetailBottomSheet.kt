package com.prmto.mova_movieapp.core.presentation.detail_bottom_sheet

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import coil.load
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.prmto.mova_movieapp.R
import com.prmto.mova_movieapp.core.data.data_source.remote.ImageApi
import com.prmto.mova_movieapp.core.data.data_source.remote.ImageSize
import com.prmto.mova_movieapp.core.domain.models.Movie
import com.prmto.mova_movieapp.core.domain.models.TvSeries
import com.prmto.mova_movieapp.core.presentation.util.AlertDialogUtil
import com.prmto.mova_movieapp.core.presentation.util.UtilFunctions
import com.prmto.mova_movieapp.core.presentation.util.collectFlow
import com.prmto.mova_movieapp.databinding.FragmentDetailBottomSheetBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailBottomSheet : BottomSheetDialogFragment() {

    private var _binding: FragmentDetailBottomSheetBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DetailBottomSheetViewModel by viewModels()

    private lateinit var utilFunctions: UtilFunctions

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentDetailBottomSheetBinding.inflate(inflater, container, false)
        _binding = binding
        utilFunctions = UtilFunctions()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupButtonClickListeners()

        collectData()
    }

    private fun collectData() {
        collectUiEvent()

        collectFlow(viewModel.state) { state ->
            if (state.movie != null) {
                bindMovie(movie = state.movie)
            }
            if (state.tvSeries != null) {
                bindTvSeries(tvSeries = state.tvSeries)
            }
            utilFunctions.setAddFavoriteIcon(
                doesAddFavorite = state.doesAddFavorite,
                imageButton = binding.btnFavoriteList
            )
            utilFunctions.setAddWatchListIcon(
                doesAddWatchList = state.doesAddWatchList,
                imageButton = binding.btnWatchingList
            )
        }
    }

    private fun collectUiEvent() {
        collectFlow(viewModel.uiEvent) { uiEvent ->
            when (uiEvent) {
                is DetailBottomUiEvent.NavigateTo -> {
                    findNavController().navigate(uiEvent.directions)
                }

                is DetailBottomUiEvent.PopBackStack -> {
                    findNavController().popBackStack()
                }

                is DetailBottomUiEvent.ShowSnackbar -> {
                    return@collectFlow
                }

                is DetailBottomUiEvent.ShowAlertDialog -> {
                    AlertDialogUtil.showAlertDialog(
                        context = requireContext(),
                        title = R.string.sign_in,
                        message = R.string.must_login_able_to_add_in_list,
                        positiveBtnMessage = R.string.sign_in,
                        negativeBtnMessage = R.string.cancel,
                        onClickPositiveButton = {
                            findNavController().navigate(DetailBottomSheetDirections.actionDetailBottomSheetToLoginFragment())
                        }
                    )
                }
            }
        }
    }
    private fun bindMovie(movie: Movie) {
        binding.apply {
            tvName.text = if (movie.title == movie.originalTitle) {
                movie.title
            } else {
                getString(
                    R.string.tv_name_with_original_name,
                    movie.title,
                    movie.originalTitle
                )
            }
            tvReleaseDate.text = movie.releaseDate
            tvOverview.text = movie.overview
            if (movie.posterPath != null) {
                loadImage(posterPath = movie.posterPath)
            }
            tvBottomInfoText.text =
                requireContext().getString(R.string.detail_bottom_sheet_movie_info)
        }
    }

    private fun bindTvSeries(tvSeries: TvSeries) {
        binding.apply {
            tvName.text = if (tvSeries.name == tvSeries.originalName) {
                tvSeries.name
            } else {
                getString(
                    R.string.tv_name_with_original_name,
                    tvSeries.name,
                    tvSeries.originalName
                )
            }
            tvOverview.text = tvSeries.overview
            tvReleaseDate.text = tvSeries.firstAirDate
            if (tvSeries.posterPath != null) {
                loadImage(posterPath = tvSeries.posterPath)
            }
            tvBottomInfoText.text = requireContext().getString(R.string.detail_bottom_sheet_tv_info)

            tvOverview.movementMethod = ScrollingMovementMethod()
        }
    }

    private fun loadImage(posterPath: String) {
        binding.ivPoster.load(
            ImageApi.getImage(
                imageSize = ImageSize.W185.path,
                imageUrl = posterPath
            )
        )
    }

    private fun setupButtonClickListeners() {
        binding.apply {
            detailSection.setOnClickListener {
                viewModel.onEvent(DetailBottomSheetEvent.NavigateToDetailFragment)
            }
            ibClose.setOnClickListener {
                viewModel.onEvent(DetailBottomSheetEvent.Close)
            }
            btnFavoriteList.setOnClickListener {
                viewModel.onEvent(DetailBottomSheetEvent.ClickedAddFavoriteList)
            }
            btnWatchingList.setOnClickListener {
                viewModel.onEvent(DetailBottomSheetEvent.ClickedAddWatchList)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}