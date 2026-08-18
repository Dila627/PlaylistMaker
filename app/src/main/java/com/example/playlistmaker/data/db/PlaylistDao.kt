package com.example.playlistmaker.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    // =========================================================
    // PLAYLIST
    // =========================================================

    @Insert
    suspend fun insertPlaylist(
        playlist: PlaylistEntity
    ): Long

    @Update
    suspend fun updatePlaylist(
        playlist: PlaylistEntity
    )

    @Query(
        """
        SELECT *
        FROM playlists
        ORDER BY id DESC
        """
    )
    fun getPlaylists(): Flow<List<PlaylistEntity>>

    @Query(
        """
        SELECT *
        FROM playlists
        WHERE id = :playlistId
        LIMIT 1
        """
    )
    suspend fun getPlaylistById(
        playlistId: Long
    ): PlaylistEntity?

    // =========================================================
    // INSERT TRACK
    // =========================================================

    @Insert(
        onConflict = OnConflictStrategy.IGNORE
    )
    suspend fun insertTrack(
        track: PlaylistTrackEntity
    ): Long

    // =========================================================
    // TRACK EXISTS IN CURRENT PLAYLIST
    // =========================================================

    @Query(
        """
        SELECT EXISTS(
            SELECT 1
            FROM playlist_tracks
            WHERE playlistId = :playlistId
            AND trackId = :trackId
        )
        """
    )
    suspend fun isTrackInPlaylist(
        playlistId: Long,
        trackId: Long
    ): Boolean

    // =========================================================
    // TRACK EXISTS IN ANOTHER PLAYLIST
    // =========================================================

    /*
     * Sprint 23:
     *
     * Проверяем, находится ли этот же трек
     * хотя бы в одном другом плейлисте.
     */
    @Query(
        """
        SELECT EXISTS(
            SELECT 1
            FROM playlist_tracks
            WHERE trackId = :trackId
            AND playlistId != :playlistId
        )
        """
    )
    suspend fun isTrackInOtherPlaylists(
        trackId: Long,
        playlistId: Long
    ): Boolean

    // =========================================================
    // TRACK COUNT
    // =========================================================

    @Query(
        """
        SELECT COUNT(*)
        FROM playlist_tracks
        WHERE playlistId = :playlistId
        """
    )
    suspend fun getTracksCount(
        playlistId: Long
    ): Int

    // =========================================================
    // GET PLAYLIST TRACKS
    // =========================================================

    /*
     * Последний добавленный трек находится сверху.
     */
    @Query(
        """
        SELECT *
        FROM playlist_tracks
        WHERE playlistId = :playlistId
        ORDER BY rowid DESC
        """
    )
    suspend fun getPlaylistTracks(
        playlistId: Long
    ): List<PlaylistTrackEntity>

    // =========================================================
    // DELETE TRACK FROM ONE PLAYLIST
    // =========================================================

    @Query(
        """
        DELETE FROM playlist_tracks
        WHERE playlistId = :playlistId
        AND trackId = :trackId
        """
    )
    suspend fun deleteTrackFromPlaylist(
        playlistId: Long,
        trackId: Long
    )

    // =========================================================
    // DELETE ALL TRACKS OF PLAYLIST
    // =========================================================

    @Query(
        """
        DELETE FROM playlist_tracks
        WHERE playlistId = :playlistId
        """
    )
    suspend fun deleteAllPlaylistTracks(
        playlistId: Long
    )

    // =========================================================
    // DELETE PLAYLIST
    // =========================================================

    @Query(
        """
        DELETE FROM playlists
        WHERE id = :playlistId
        """
    )
    suspend fun deletePlaylist(
        playlistId: Long
    )
}