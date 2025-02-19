package com.prmto.mova_movieapp.feature_home.presentation.home


import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.prmto.mova_movieapp.R
import com.prmto.mova_movieapp.core.domain.models.Movie
import com.prmto.mova_movieapp.core.domain.repository.isAvaliable
import com.prmto.mova_movieapp.core.presentation.util.BaseUiEvent
import com.prmto.mova_movieapp.core.presentation.util.UiText
import com.prmto.mova_movieapp.core.presentation.util.addOnBackPressedCallback
import com.prmto.mova_movieapp.core.presentation.util.asString
import com.prmto.mova_movieapp.core.presentation.util.collectFlow
import com.prmto.mova_movieapp.core.presentation.util.collectFlowInside
import com.prmto.mova_movieapp.core.presentation.util.isEmpty
import com.prmto.mova_movieapp.core.util.getCountryIsoCode
import com.prmto.mova_movieapp.core.util.handlePagingLoadState.HandlePagingLoadStateMovieAndTvBaseRecyclerAdapter
import com.prmto.mova_movieapp.core.util.handlePagingLoadState.HandlePagingStateNowPlayingRecyclerAdapter
import com.prmto.mova_movieapp.databinding.FragmentHomeBinding
import com.prmto.mova_movieapp.feature_home.presentation.home.adapter.NowPlayingRecyclerAdapter
import com.prmto.mova_movieapp.feature_home.presentation.home.adapter.PopularMoviesAdapter
import com.prmto.mova_movieapp.feature_home.presentation.home.adapter.PopularTvSeriesAdapter
import com.prmto.mova_movieapp.feature_home.presentation.home.adapter.TopRatedMoviesAdapter
import com.prmto.mova_movieapp.feature_home.presentation.home.adapter.TopRatedTvSeriesAdapter
import com.prmto.mova_movieapp.feature_home.presentation.home.event.HomeAdapterLoadStateEvent
import com.prmto.mova_movieapp.feature_home.presentation.home.event.HomeEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay


@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val nowPlayingAdapter: NowPlayingRecyclerAdapter by lazy { NowPlayingRecyclerAdapter() }

    private val popularMoviesAdapter: PopularMoviesAdapter by lazy { PopularMoviesAdapter() }

    private val popularTvSeriesAdapter: PopularTvSeriesAdapter by lazy { PopularTvSeriesAdapter() }

    private val topRatedMoviesAdapter: TopRatedMoviesAdapter by lazy { TopRatedMoviesAdapter() }

    private val topRatedTvSeriesAdapter: TopRatedTvSeriesAdapter by lazy { TopRatedTvSeriesAdapter() }

    private val viewModel: HomeViewModel by viewModels()

    private var job: Job? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentHomeBinding.bind(view)
        _binding = binding

        handlePagingLoadStates()
        observeConnectivityStatus()
        collectHomeUiEventsAndLoadState()
        updateCountryIsoCode()
        setupRecyclerAdapters()
        setAdaptersClickListener()
        setupListenerSeeAllClickEvents()

        addOnBackPressedCallback(
            activity = requireActivity(),
            onBackPressed = {
                viewModel.onEvent(HomeEvent.OnBackPressed)
            }
        )
        setupClickListener()
    }

    private fun setupClickListener() {
        binding.btnNavigateUp.setOnClickListener {
            viewModel.onEvent(HomeEvent.NavigateUpFromSeeAllSection)
        }
        binding.btnError.setOnClickListener {
            if (viewModel.isNetworkAvaliable()) {
                collectDataLifecycleAware()
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.internet_error),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun observeConnectivityStatus() {
        collectFlow(viewModel.networkState) { networkState ->
            delay(20)
            if (networkState.isAvaliable()) {
                job?.cancel()
                showScrollView()
                hideErrorScreen()
                collectDataLifecycleAware()
            } else {
                job?.cancel()
                if (isEmptyAdapters()) {
                    hideScrollView()
                    showErrorScreen()
                }
            }

        }
    }

    private fun isEmptyAdapters(): Boolean {
        return nowPlayingAdapter.itemCount <= 0 || popularMoviesAdapter.isEmpty() || popularTvSeriesAdapter.isEmpty() || topRatedMoviesAdapter.isEmpty() || topRatedTvSeriesAdapter.isEmpty()
    }

    private fun showScrollView() {
        binding.scrollView.isVisible = true
    }

    private fun hideScrollView() {
        binding.scrollView.isVisible = false
    }

    private fun showErrorScreen() {
        binding.errorScreen.isVisible = true
    }

    private fun hideErrorScreen() {
        binding.errorScreen.isVisible = false
    }

    private fun collectHomeUiEventsAndLoadState() {

        collectFlow(viewModel.eventFlow) { uiEvent ->
            when (uiEvent) {
                is BaseUiEvent.NavigateTo -> {
                    findNavController().navigate(
                        uiEvent.directions
                    )
                }

                is BaseUiEvent.ShowSnackbar -> {
                    Snackbar.make(
                        requireView(),
                        uiEvent.uiText.asString(requireContext()),
                        Snackbar.LENGTH_LONG
                    ).show()
                }

                else -> return@collectFlow
            }
        }

        collectFlow(viewModel.adapterLoadState) {
            binding.nowPlayingShimmerLayout.isVisible = it.nowPlayingState.isLoading
            binding.popularMoviesShimmerLayout.isVisible =
                it.popularMoviesState.isLoading
            binding.popularTvSeriesShimmerLayout.isVisible =
                it.popularTvSeriesState.isLoading
            binding.topRatedMoviesShimmerLayout.isVisible =
                it.topRatedMoviesState.isLoading
            binding.topRatedTvSeriesShimmerLayout.isVisible =
                it.topRatedTvSeriesState.isLoading
        }
    }

    override fun onStart() {
        super.onStart()
        if (viewModel.homeState.value.isShowsSeeAllPage) {
            val context = requireContext()
            val adapter =
                when (viewModel.homeState.value.seeAllPageToolBarText?.asString(context)) {
                    context.getString(R.string.now_playing) -> {
                        nowPlayingAdapter
                    }

                    context.getString(R.string.popular_movies) -> {
                        popularMoviesAdapter
                    }

                    context.getString(R.string.popular_tv_series) -> {
                        popularTvSeriesAdapter
                    }

                    context.getString(R.string.top_rated_movies) -> {
                        topRatedMoviesAdapter
                    }

                    context.getString(R.string.top_rated_tv_series) -> {
                        topRatedTvSeriesAdapter
                    }

                    else -> nowPlayingAdapter
                }
            binding.recyclerViewSeeAll.adapter = adapter
        }
    }

    private fun handlePagingLoadStates() {

        HandlePagingStateNowPlayingRecyclerAdapter(
            nowPlayingRecyclerAdapter = nowPlayingAdapter,
            onLoading = { viewModel.onAdapterLoadStateEvent(HomeAdapterLoadStateEvent.NowPlayingLoading) },
            onNotLoading = { viewModel.onAdapterLoadStateEvent(HomeAdapterLoadStateEvent.NowPlayingNotLoading) },
            onError = { eventToPagingError(it) }
        )

        HandlePagingLoadStateMovieAndTvBaseRecyclerAdapter<Movie>(
            pagingAdapter = popularMoviesAdapter,
            onLoading = { viewModel.onAdapterLoadStateEvent(HomeAdapterLoadStateEvent.PopularMoviesLoading) },
            onNotLoading = { viewModel.onAdapterLoadStateEvent(HomeAdapterLoadStateEvent.PopularMoviesNotLoading) },
            onError = { eventToPagingError(it) }
        )

        HandlePagingLoadStateMovieAndTvBaseRecyclerAdapter(
            pagingAdapter = topRatedMoviesAdapter,
            onLoading = { viewModel.onAdapterLoadStateEvent(HomeAdapterLoadStateEvent.TopRatedMoviesLoading) },
            onNotLoading = { viewModel.onAdapterLoadStateEvent(HomeAdapterLoadStateEvent.TopRatedMoviesNotLoading) },
            onError = { eventToPagingError(it) }
        )

        HandlePagingLoadStateMovieAndTvBaseRecyclerAdapter(
            pagingAdapter = popularTvSeriesAdapter,
            onLoading = { viewModel.onAdapterLoadStateEvent(HomeAdapterLoadStateEvent.PopularTvSeriesLoading) },
            onNotLoading = { viewModel.onAdapterLoadStateEvent(HomeAdapterLoadStateEvent.PopularTvSeriesNotLoading) },
            onError = { eventToPagingError(it) }
        )

        HandlePagingLoadStateMovieAndTvBaseRecyclerAdapter(
            pagingAdapter = topRatedTvSeriesAdapter,
            onLoading = { viewModel.onAdapterLoadStateEvent(HomeAdapterLoadStateEvent.TopRatedTvSeriesLoading) },
            onNotLoading = { viewModel.onAdapterLoadStateEvent(HomeAdapterLoadStateEvent.TopRatedTvSeriesNotLoading) },
            onError = { eventToPagingError(it) }
        )
    }

    private fun eventToPagingError(uiText: UiText) {
        viewModel.onAdapterLoadStateEvent(HomeAdapterLoadStateEvent.PagingError(uiText))
    }

    private fun updateCountryIsoCode() {
        val countryIsoCode = requireContext().getCountryIsoCode().uppercase()
        viewModel.onEvent(HomeEvent.UpdateCountryIsoCode(countryIsoCode))
    }

    private fun collectDataLifecycleAware() {
        job = collectFlowInside {
            collectFlow(viewModel.homeState) { homeState ->
                binding.apply {
                    seeAllPage.isVisible = homeState.isShowsSeeAllPage
                    scrollView.isVisible = !homeState.isShowsSeeAllPage
                    if (homeState.isShowsSeeAllPage) {
                        showSeeAllPage(homeState.seeAllPageToolBarText)
                    } else {
                        hideSeeAllPage()
                    }
                }
            }
            collectFlow(viewModel.getNowPlayingMovies()) { pagingData ->
                nowPlayingAdapter.submitData(pagingData)
            }
            collectFlow(viewModel.getPopularMovies()) { pagingData ->
                popularMoviesAdapter.submitData(pagingData)
            }
            collectFlow(viewModel.getTopRatedMovies()) { pagingData ->
                topRatedMoviesAdapter.submitData(pagingData)
            }
            collectFlow(viewModel.getPopularTvSeries()) { pagingData ->
                popularTvSeriesAdapter.submitData(pagingData)
            }
            collectFlow(viewModel.getTopRatedTvSeries()) { pagingData ->
                topRatedTvSeriesAdapter.submitData(pagingData)
            }
        }
    }

    private fun showSeeAllPage(uiText: UiText?) {
        binding.apply {
            seeAllPage.animation = slideInLeftAnim()
            uiText?.let {
                toolbarText.text = it.asString(requireContext())
            }
        }
    }

    private fun hideSeeAllPage() {
        binding.apply {
            scrollView.animation = slideInLeftAnim()
            recyclerViewSeeAll.removeAllViews()
        }
    }

    private fun setupListenerSeeAllClickEvents() {
        binding.apply {

            nowPlayingSeeAll.setOnClickListener {
                viewModel.onEvent(
                    HomeEvent.ClickSeeAllText(UiText.StringResource(R.string.now_playing))
                )
                recyclerViewSeeAll.adapter = nowPlayingAdapter
            }

            popularMoviesSeeAll.setOnClickListener {
                viewModel.onEvent(
                    HomeEvent.ClickSeeAllText(UiText.StringResource(R.string.popular_movies))
                )
                recyclerViewSeeAll.adapter = popularMoviesAdapter
            }

            popularTvSeeAll.setOnClickListener {
                viewModel.onEvent(
                    HomeEvent.ClickSeeAllText(UiText.StringResource(R.string.popular_tv_series))
                )
                recyclerViewSeeAll.adapter = popularTvSeriesAdapter
            }

            topRatedMoviesSeeAll.setOnClickListener {
                viewModel.onEvent(
                    HomeEvent.ClickSeeAllText(UiText.StringResource(R.string.top_rated_movies))
                )
                recyclerViewSeeAll.adapter = topRatedMoviesAdapter
            }

            topRatedTvSeriesSeeAll.setOnClickListener {
                viewModel.onEvent(
                    HomeEvent.ClickSeeAllText(UiText.StringResource(R.string.top_rated_tv_series))
                )
                recyclerViewSeeAll.adapter = topRatedTvSeriesAdapter
            }
        }

    }

    private fun slideInLeftAnim(): Animation =
        AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_left)

    private fun setupRecyclerAdapters() {
        binding.apply {
            nowPlayingRecyclerView.adapter = nowPlayingAdapter
            nowPlayingRecyclerView.setAlpha(true)
            popularMoviesRecyclerView.adapter = popularMoviesAdapter
            topRatedMoviesRecyclerView.adapter = topRatedMoviesAdapter
            popularTvSeriesRecyclerView.adapter = popularTvSeriesAdapter
            topRatedTvSeriesRecyclerView.adapter = topRatedTvSeriesAdapter
        }
    }

    private fun setAdaptersClickListener() {
        val action = HomeFragmentDirections.actionHomeFragmentToDetailBottomSheet(null, null)
        popularMoviesAdapter.setOnItemClickListener { movie ->
            action.movie = movie
            action.tvSeries = null
            viewModel.onEvent(HomeEvent.NavigateToDetailBottomSheet(action))
        }

        topRatedMoviesAdapter.setOnItemClickListener { movie ->
            action.movie = movie
            action.tvSeries = null
            viewModel.onEvent(HomeEvent.NavigateToDetailBottomSheet(action))
        }

        nowPlayingAdapter.setOnClickListener { movie ->
            action.movie = movie
            action.tvSeries = null
            viewModel.onEvent(HomeEvent.NavigateToDetailBottomSheet(action))
        }

        popularTvSeriesAdapter.setOnItemClickListener { tvSeries ->
            action.tvSeries = tvSeries
            action.movie = null
            viewModel.onEvent(HomeEvent.NavigateToDetailBottomSheet(action))
        }

        topRatedTvSeriesAdapter.setOnItemClickListener { tvSeries ->
            action.tvSeries = tvSeries
            action.movie = null
            viewModel.onEvent(HomeEvent.NavigateToDetailBottomSheet(action))
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}