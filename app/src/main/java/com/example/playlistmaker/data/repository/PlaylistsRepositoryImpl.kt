package com.example.playlistmaker.data.repository

import com.example.playlistmaker.data.db.PlaylistDao
import com.example.playlistmaker.data.db.PlaylistEntity
import com.example.playlistmaker.data.db.PlaylistTrackEntity
import com.example.playlistmaker.domain.api.PlaylistsRepository
import com.example.playlistmaker.domain.models.Playlist
import com.example.playlistmaker.domain.models.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class PlaylistsRepositoryImpl(
    private val playlistDao: PlaylistDao
) : PlaylistsRepository {

    // =========================================================
    // CREATE PLAYLIST
    // =========================================================

    override suspend fun createPlaylist(
        playlist: Playlist
    ): Long {

        return playlistDao.insertPlaylist(
            mapPlaylistToEntity(
                playlist
            )
        )
    }

    // =========================================================
    // GET PLAYLISTS
    // =========================================================

    override fun getPlaylists():
            Flow<List<Playlist>> {

        return playlistDao
            .getPlaylists()
            .map { entities ->

                entities.map { entity ->

                    mapPlaylistToDomain(
                        entity
                    )
                }
            }
            .distinctUntilChanged()
    }

    // =========================================================
    // ADD TRACK TO PLAYLIST
    // =========================================================

    override suspend fun addTrackToPlaylist(
        track: Track,
        playlist: Playlist
    ): Boolean {

        val isAlreadyAdded =
            playlistDao.isTrackInPlaylist(
                playlistId = playlist.id,
                trackId = track.trackId
            )

        if (isAlreadyAdded) {
            return false
        }

        val insertResult =
            playlistDao.insertTrack(
                mapTrackToEntity(
                    track = track,
                    playlistId = playlist.id
                )
            )

        if (insertResult == -1L) {
            return false
        }

        val playlistEntity =
            playlistDao.getPlaylistById(
                playlist.id
            )
                ?: return true

        val currentTrackIds =
            parseTrackIds(
                playlistEntity.trackIds
            )

        /*
         * Новый трек добавляется в начало,
         * чтобы сохранять порядок
         * "последний добавленный сверху".
         */
        val updatedTrackIds =
            listOf(
                track.trackId
            ) + currentTrackIds

        val updatedTracksCount =
            playlistDao.getTracksCount(
                playlist.id
            )

        playlistDao.updatePlaylist(
            playlistEntity.copy(
                trackIds =
                    updatedTrackIds
                        .joinToString(
                            ","
                        ),

                tracksCount =
                    updatedTracksCount
            )
        )

        return true
    }

    // =========================================================
    // GET PLAYLIST TRACKS
    // =========================================================

    override suspend fun getPlaylistTracks(
        playlistId: Long
    ): List<Track> {

        return playlistDao
            .getPlaylistTracks(
                playlistId
            )
            .map { entity ->

                mapTrackToDomain(
                    entity
                )
            }
    }

    // =========================================================
    // DELETE TRACK FROM PLAYLIST
    // =========================================================

    override suspend fun deleteTrackFromPlaylist(
        playlistId: Long,
        trackId: Long
    ) {

        /*
         * Sprint 23.
         *
         * До удаления проверяем,
         * используется ли такой же трек
         * в каком-либо другом плейлисте.
         */
        val isTrackInOtherPlaylists =
            playlistDao
                .isTrackInOtherPlaylists(
                    trackId = trackId,
                    playlistId = playlistId
                )

        /*
         * Удаляем только связь/запись
         * текущего плейлиста.
         *
         * Благодаря условию playlistId + trackId
         * запись этого же трека в другом
         * плейлисте не удаляется.
         */
        playlistDao
            .deleteTrackFromPlaylist(
                playlistId = playlistId,
                trackId = trackId
            )

        /*
         * В текущей Room-модели отдельной
         * общей таблицы Track для плейлистов нет.
         *
         * PlaylistTrackEntity содержит данные
         * отдельно для каждого playlistId.
         *
         * Поэтому:
         * - если трек есть в другом плейлисте,
         *   его запись там автоматически остаётся;
         *
         * - если трека больше нигде нет,
         *   удалённая выше запись была его
         *   последней playlist-записью.
         *
         * Переменную используем явно,
         * чтобы логика проверки была частью
         * Repository согласно критерию Sprint 23.
         */
        if (isTrackInOtherPlaylists) {
            // Трек остаётся сохранён в другом плейлисте.
        }

        // =====================================================
        // UPDATE CURRENT PLAYLIST DATA
        // =====================================================

        val playlistEntity =
            playlistDao.getPlaylistById(
                playlistId
            )
                ?: return

        val currentTrackIds =
            parseTrackIds(
                playlistEntity.trackIds
            )

        val updatedTrackIds =
            currentTrackIds.filter { id ->

                id != trackId
            }

        val updatedTracksCount =
            playlistDao.getTracksCount(
                playlistId
            )

        playlistDao.updatePlaylist(
            playlistEntity.copy(

                trackIds =
                    updatedTrackIds
                        .joinToString(
                            ","
                        ),

                tracksCount =
                    updatedTracksCount
            )
        )
    }

    // =========================================================
    // UPDATE PLAYLIST
    // =========================================================

    override suspend fun updatePlaylist(
        playlist: Playlist
    ) {

        playlistDao.updatePlaylist(
            mapPlaylistToEntity(
                playlist
            )
        )
    }

    // =========================================================
    // DELETE PLAYLIST
    // =========================================================

    override suspend fun deletePlaylist(
        playlistId: Long
    ) {

        /*
         * Удаляем записи треков
         * только этого плейлиста.
         */
        playlistDao
            .deleteAllPlaylistTracks(
                playlistId
            )

        playlistDao
            .deletePlaylist(
                playlistId
            )
    }

    // =========================================================
    // PLAYLIST -> ENTITY
    // =========================================================

    private fun mapPlaylistToEntity(
        playlist: Playlist
    ): PlaylistEntity {

        return PlaylistEntity(

            id =
                playlist.id,

            name =
                playlist.name,

            description =
                playlist.description,

            imagePath =
                playlist.imagePath,

            trackIds =
                playlist.trackIds
                    .joinToString(
                        ","
                    ),

            tracksCount =
                playlist.tracksCount
        )
    }

    // =========================================================
    // ENTITY -> PLAYLIST
    // =========================================================

    private fun mapPlaylistToDomain(
        entity: PlaylistEntity
    ): Playlist {

        return Playlist(

            id =
                entity.id,

            name =
                entity.name,

            description =
                entity.description,

            imagePath =
                entity.imagePath,

            trackIds =
                parseTrackIds(
                    entity.trackIds
                ),

            tracksCount =
                entity.tracksCount
        )
    }

    // =========================================================
    // TRACK -> ENTITY
    // =========================================================

    private fun mapTrackToEntity(
        track: Track,
        playlistId: Long
    ): PlaylistTrackEntity {

        return PlaylistTrackEntity(

            playlistId =
                playlistId,

            trackId =
                track.trackId,

            trackName =
                track.trackName,

            artistName =
                track.artistName,

            trackTimeMillis =
                track.trackTimeMillis,

            artworkUrl100 =
                track.artworkUrl100,

            collectionName =
                track.collectionName,

            releaseDate =
                track.releaseDate,

            primaryGenreName =
                track.primaryGenreName,

            country =
                track.country,

            previewUrl =
                track.previewUrl
        )
    }

    // =========================================================
    // ENTITY -> TRACK
    // =========================================================

    private fun mapTrackToDomain(
        entity: PlaylistTrackEntity
    ): Track {

        return Track(

            trackId =
                entity.trackId,

            trackName =
                entity.trackName,

            artistName =
                entity.artistName,

            trackTimeMillis =
                entity.trackTimeMillis,

            artworkUrl100 =
                entity.artworkUrl100,

            collectionName =
                entity.collectionName,

            releaseDate =
                entity.releaseDate,

            primaryGenreName =
                entity.primaryGenreName,

            country =
                entity.country,

            previewUrl =
                entity.previewUrl
        )
    }

    // =========================================================
    // STRING -> LIST<Long>
    // =========================================================

    private fun parseTrackIds(
        value: String
    ): List<Long> {

        if (value.isBlank()) {
            return emptyList()
        }

        return value
            .split(",")
            .mapNotNull { trackId ->

                trackId.toLongOrNull()
            }
    }
}