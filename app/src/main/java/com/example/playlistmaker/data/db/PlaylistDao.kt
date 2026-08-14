package com.example.playlistmaker.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    @Insert
    suspend fun insertPlaylist(
        playlist: PlaylistEntity
    ): Long

    @Update
    suspend fun updatePlaylist(
        playlist: PlaylistEntity
    )

    @Query("SELECT * FROM playlists ORDER BY id DESC")
    fun getPlaylists(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists WHERE id = :playlistId LIMIT 1")
    suspend fun getPlaylistById(
        playlistId: Long
    ): PlaylistEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTrack(
        track: PlaylistTrackEntity
    ): Long

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

    @Query(
        """
    DELETE FROM playlist_tracks
    WHERE playlistId = :playlistId
    """
    )
    suspend fun deleteAllPlaylistTracks(
        playlistId: Long
    )

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

