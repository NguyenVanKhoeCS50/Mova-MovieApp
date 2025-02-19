package com.prmto.mova_movieapp.feature_movie_tv_detail.data.mapper

import com.prmto.mova_movieapp.feature_movie_tv_detail.data.dto.detail.video.VideosDto
import com.prmto.mova_movieapp.feature_movie_tv_detail.domain.models.detail.video.Videos

fun VideosDto.toVideo(): Videos {
    return Videos(
        id = id,
        result = videoResultDto.map { it.toVideoResult() }
    )
}