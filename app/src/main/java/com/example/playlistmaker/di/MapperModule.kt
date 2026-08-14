package com.example.playlistmaker.di

import com.example.playlistmaker.data.mapper.TrackDbMapper
import org.koin.dsl.module

val mapperModule = module {

    single {
        TrackDbMapper()
    }

}