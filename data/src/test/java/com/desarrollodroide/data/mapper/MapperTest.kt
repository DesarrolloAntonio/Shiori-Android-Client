package com.desarrollodroide.data.mapper

import com.desarrollodroide.data.local.room.entity.BookmarkEntity
import com.desarrollodroide.data.local.room.entity.TagEntity
import com.desarrollodroide.model.Account
import com.desarrollodroide.model.Tag
import com.desarrollodroide.model.UpdateCachePayload
import org.junit.jupiter.api.Assertions.*
import com.desarrollodroide.network.model.AccountDTO
import com.desarrollodroide.network.model.BookmarkDTO
import com.desarrollodroide.network.model.BookmarksDTO
import com.desarrollodroide.network.model.LivenessResponseDTO
import com.desarrollodroide.network.model.LoginResponseDTO
import com.desarrollodroide.network.model.LoginResponseMessageDTO
import com.desarrollodroide.network.model.ReadableContentResponseDTO
import com.desarrollodroide.network.model.ReadableMessageDto
import com.desarrollodroide.network.model.ReleaseInfoDTO
import com.desarrollodroide.network.model.SessionDTO
import com.desarrollodroide.network.model.TagDTO
import org.junit.jupiter.api.Test

class MapperTest {

    @Test
    fun `SessionDTO toDomainModel maps correctly`() {
        val accountDTO = AccountDTO(
            id = 1,
            userName = "testUser",
            password = "password",
            isOwner = true,
            oldPassword = "oldPass",
            newPassword = "newPass",
            isLegacyApi = true
        )
        val sessionDTO = SessionDTO(
            token = "token123",
            session = "session123",
            account = accountDTO
        )

        val user = sessionDTO.toDomainModel()

        assertEquals("token123", user.token)
        assertEquals("session123", user.session)
        assertEquals("testUser", user.account.userName)
        assertEquals("password", user.account.password)
        assertEquals(true, user.account.owner)
        assertEquals(true, user.account.isLegacyApi)  // Corrected to match the DTO's isLegacyApi value
    }

    @Test
    fun `SessionDTO toProtoEntity maps correctly`() {
        val accountDTO = AccountDTO(
            id = 1,
            userName = "testUser",
            password = "password",
            isOwner = true,
            oldPassword = "oldPass",
            newPassword = "newPass",
            isLegacyApi = true
        )
        val sessionDTO = SessionDTO(
            token = "token123",
            session = "session123",
            account = accountDTO
        )

        val userPreferences = sessionDTO.toProtoEntity()

        assertEquals(1, userPreferences.id)
        assertEquals("testUser", userPreferences.username)
        assertEquals(true, userPreferences.owner)
        assertEquals("", userPreferences.password)
        assertEquals("session123", userPreferences.session)
        assertEquals("", userPreferences.url)  // Assuming this is not set from DTO
        assertEquals(false, userPreferences.rememberPassword)  // Assuming default value
        assertEquals(true, userPreferences.isLegacyApi)
        assertEquals("", userPreferences.token)
    }

    @Test
    fun `AccountDTO toDomainModel maps correctly`() {
        val accountDTO = AccountDTO(
            id = 1,
            userName = "testUser",
            password = "password",
            isOwner = true,
            oldPassword = "oldPass",
            newPassword = "newPass",
            isLegacyApi = true
        )

        val account = accountDTO.toDomainModel()

        assertEquals("testUser", account.userName)
        assertEquals("password", account.password)
        assertEquals(true, account.owner)
        assertEquals(true, account.isLegacyApi)  // Corrected to match the DTO's isLegacyApi value
    }

    @Test
    fun `BookmarkDTO toDomainModel maps correctly`() {
        val tagDTO = TagDTO(
            id = 1,
            name = "tag1",
            nBookmarks = 5
        )

        val bookmarkDTO = BookmarkDTO(
            id = 1,
            url = "http://example.com",
            title = "Example Title",
            excerpt = "Example Excerpt",
            author = "Author Name",
            public = 1,
            modified = "2023-06-18",
            imageURL = "/image.jpg",
            hasContent = true,
            hasArchive = true,
            hasEbook = true,
            tags = listOf(tagDTO),
            createArchive = true,
            createEbook = true
        )

        val serverUrl = "http://example.com"
        val bookmark = bookmarkDTO.toDomainModel(serverUrl)

        assertEquals(1, bookmark.id)
        assertEquals("http://example.com", bookmark.url)
        assertEquals("Example Title", bookmark.title)
        assertEquals("Example Excerpt", bookmark.excerpt)
        assertEquals("Author Name", bookmark.author)
        assertEquals(1, bookmark.public)
        assertEquals("2023-06-18", bookmark.modified)
        assertEquals("http://example.com/image.jpg", bookmark.imageURL)
        assertEquals(true, bookmark.hasContent)
        assertEquals(true, bookmark.hasArchive)
        assertEquals(true, bookmark.hasEbook)
        assertEquals(1, bookmark.tags.size)
        assertEquals(1, bookmark.tags[0].id)
        assertEquals("tag1", bookmark.tags[0].name)
        assertEquals(5, bookmark.tags[0].nBookmarks)
        assertEquals(true, bookmark.createArchive)
        assertEquals(true, bookmark.createEbook)
    }


    @Test
    fun `BookmarksDTO toDomainModel maps correctly`() {
        val tagDTO = TagDTO(
            id = 1,
            name = "tag1",
            nBookmarks = 5
        )

        val bookmarkDTO = BookmarkDTO(
            id = 1,
            url = "http://example.com",
            title = "Example Title",
            excerpt = "Example Excerpt",
            author = "Author Name",
            public = 1,
            modified = "2023-06-18",
            imageURL = "/image.jpg",
            hasContent = true,
            hasArchive = true,
            hasEbook = true,
            tags = listOf(tagDTO),
            createArchive = true,
            createEbook = true
        )

        val bookmarksDTO = BookmarksDTO(
            page = 1,
            maxPage = 10,
            bookmarks = listOf(bookmarkDTO)
        )

        val serverUrl = "http://example.com"
        val bookmarks = bookmarksDTO.toDomainModel(serverUrl)

        assertEquals(1, bookmarks.page)
        assertEquals(10, bookmarks.maxPage)
        assertEquals(1, bookmarks.bookmarks.size)

        val bookmark = bookmarks.bookmarks[0]
        assertEquals(1, bookmark.id)
        assertEquals("http://example.com", bookmark.url)
        assertEquals("Example Title", bookmark.title)
        assertEquals("Example Excerpt", bookmark.excerpt)
        assertEquals("Author Name", bookmark.author)
        assertEquals(1, bookmark.public)
        assertEquals("2023-06-18", bookmark.modified)
        assertEquals("http://example.com/image.jpg", bookmark.imageURL)
        assertEquals(true, bookmark.hasContent)
        assertEquals(true, bookmark.hasArchive)
        assertEquals(true, bookmark.hasEbook)
        assertEquals(1, bookmark.tags.size)
        assertEquals(1, bookmark.tags[0].id)
        assertEquals("tag1", bookmark.tags[0].name)
        assertEquals(5, bookmark.tags[0].nBookmarks)
        assertEquals(true, bookmark.createArchive)
        assertEquals(true, bookmark.createEbook)
    }

    @Test
    fun `TagDTO toDomainModel maps correctly`() {
        val tagDTO = TagDTO(
            id = 1,
            name = "tag1",
            nBookmarks = 5
        )

        val tag = tagDTO.toDomainModel()

        assertEquals(1, tag.id)
        assertEquals("tag1", tag.name)
        assertEquals(false, tag.selected) // Assuming selected is always false in the domain model
        assertEquals(5, tag.nBookmarks)
    }

    @Test
    fun `TagDTO toDomainModel with null fields maps correctly`() {
        val tagDTO = TagDTO(
            id = null,
            name = null,
            nBookmarks = null
        )

        val tag = tagDTO.toDomainModel()

        assertEquals(0, tag.id) // Default value for id
        assertEquals("", tag.name) // Default value for name
        assertEquals(false, tag.selected) // Assuming selected is always false in the domain model
        assertEquals(0, tag.nBookmarks) // Default value for nBookmarks
    }

    @Test
    fun `TagDTO toEntityModel maps correctly`() {
        val tagDTO = TagDTO(
            id = 1,
            name = "tag1",
            nBookmarks = 5
        )

        val tagEntity = tagDTO.toEntityModel()

        assertEquals(1, tagEntity.id)
        assertEquals("tag1", tagEntity.name)
        assertEquals(5, tagEntity.nBookmarks)
    }

    @Test
    fun `TagDTO toEntityModel with null fields maps correctly`() {
        val tagDTO = TagDTO(
            id = null,
            name = null,
            nBookmarks = null
        )

        val tagEntity = tagDTO.toEntityModel()

        assertEquals(0, tagEntity.id) // Default value for id
        assertEquals("", tagEntity.name) // Default value for name
        assertEquals(0, tagEntity.nBookmarks) // Default value for nBookmarks
    }

    @Test
    fun `TagEntity toDomainModel maps correctly`() {
        val tagEntity = TagEntity(
            id = 1,
            name = "tag1",
            nBookmarks = 5
        )

        val tag = tagEntity.toDomainModel()

        assertEquals(1, tag.id)
        assertEquals("tag1", tag.name)
        assertEquals(false, tag.selected) // Assuming selected is always false in the domain model
        assertEquals(5, tag.nBookmarks)
    }

    @Test
    fun `Account toRequestBody maps correctly`() {
        val account = Account(
            id = 1,
            userName = "testUser",
            password = "password",
            owner = true,
            serverUrl = "https://example.com",
            isLegacyApi = false
        )

        val loginRequestPayload = account.toRequestBody()

        assertEquals("testUser", loginRequestPayload.username)
        assertEquals("password", loginRequestPayload.password)
    }

    @Test
    fun `BookmarkDTO toEntityModel maps correctly`() {
        val tagDTO = TagDTO(
            id = 1,
            name = "tag1",
            nBookmarks = 5
        )

        val bookmarkDTO = BookmarkDTO(
            id = 1,
            url = "http://example.com",
            title = "Example Title",
            excerpt = "Example Excerpt",
            author = "Author Name",
            public = 1,
            modified = "2023-06-18",
            imageURL = "/image.jpg",
            hasContent = true,
            hasArchive = true,
            hasEbook = true,
            tags = listOf(tagDTO),
            createArchive = true,
            createEbook = true
        )

        val bookmarkEntity = bookmarkDTO.toEntityModel()

        assertEquals(1, bookmarkEntity.id)
        assertEquals("http://example.com", bookmarkEntity.url)
        assertEquals("Example Title", bookmarkEntity.title)
        assertEquals("Example Excerpt", bookmarkEntity.excerpt)
        assertEquals("Author Name", bookmarkEntity.author)
        assertEquals(1, bookmarkEntity.isPublic)
        assertEquals("2023-06-18", bookmarkEntity.modified)
        assertEquals("/image.jpg", bookmarkEntity.imageURL)
        assertEquals(true, bookmarkEntity.hasContent)
        assertEquals(true, bookmarkEntity.hasArchive)
        assertEquals(true, bookmarkEntity.hasEbook)
        assertEquals(1, bookmarkEntity.tags.size)
        assertEquals(1, bookmarkEntity.tags[0].id)
        assertEquals("tag1", bookmarkEntity.tags[0].name)
        assertEquals(5, bookmarkEntity.tags[0].nBookmarks)
        assertEquals(true, bookmarkEntity.createArchive)
        assertEquals(true, bookmarkEntity.createEbook)
    }

    @Test
    fun `BookmarkDTO toEntityModel with null fields maps correctly`() {
        val bookmarkDTO = BookmarkDTO(
            id = null,
            url = null,
            title = null,
            excerpt = null,
            author = null,
            public = null,
            modified = null,
            imageURL = null,
            hasContent = null,
            hasArchive = null,
            hasEbook = null,
            tags = null,
            createArchive = null,
            createEbook = null
        )

        val bookmarkEntity = bookmarkDTO.toEntityModel()

        assertEquals(0, bookmarkEntity.id) // Default value for id
        assertEquals("", bookmarkEntity.url) // Default value for url
        assertEquals("", bookmarkEntity.title) // Default value for title
        assertEquals("", bookmarkEntity.excerpt) // Default value for excerpt
        assertEquals("", bookmarkEntity.author) // Default value for author
        assertEquals(0, bookmarkEntity.isPublic) // Default value for isPublic
        assertEquals("", bookmarkEntity.modified) // Default value for modified
        assertEquals("", bookmarkEntity.imageURL) // Default value for imageURL
        assertEquals(false, bookmarkEntity.hasContent) // Default value for hasContent
        assertEquals(false, bookmarkEntity.hasArchive) // Default value for hasArchive
        assertEquals(false, bookmarkEntity.hasEbook) // Default value for hasEbook
        assertEquals(0, bookmarkEntity.tags.size) // Default empty list for tags
        assertEquals(false, bookmarkEntity.createArchive) // Default value for createArchive
        assertEquals(false, bookmarkEntity.createEbook) // Default value for createEbook
    }

    @Test
    fun `BookmarkEntity toDomainModel maps correctly`() {
        val tag = Tag(
            id = 1,
            name = "tag1",
            selected = false,
            nBookmarks = 5
        )

        val bookmarkEntity = BookmarkEntity(
            id = 1,
            url = "http://example.com",
            title = "Example Title",
            excerpt = "Example Excerpt",
            author = "Author Name",
            isPublic = 1,
            modified = "2023-06-18",
            imageURL = "/image.jpg",
            hasContent = true,
            hasArchive = true,
            hasEbook = true,
            tags = listOf(tag),
            createArchive = true,
            createEbook = true
        )

        val bookmark = bookmarkEntity.toDomainModel()

        assertEquals(1, bookmark.id)
        assertEquals("http://example.com", bookmark.url)
        assertEquals("Example Title", bookmark.title)
        assertEquals("Example Excerpt", bookmark.excerpt)
        assertEquals("Author Name", bookmark.author)
        assertEquals(1, bookmark.public)
        assertEquals("2023-06-18", bookmark.modified)
        assertEquals("/image.jpg", bookmark.imageURL)
        assertEquals(true, bookmark.hasContent)
        assertEquals(true, bookmark.hasArchive)
        assertEquals(true, bookmark.hasEbook)
        assertEquals(1, bookmark.tags.size)
        assertEquals(1, bookmark.tags[0].id)
        assertEquals("tag1", bookmark.tags[0].name)
        assertEquals(5, bookmark.tags[0].nBookmarks)
        assertEquals(true, bookmark.createArchive)
        assertEquals(true, bookmark.createEbook)
    }

    @Test
    fun `BookmarkEntity toDomainModel with empty tags maps correctly`() {
        val bookmarkEntity = BookmarkEntity(
            id = 1,
            url = "http://example.com",
            title = "Example Title",
            excerpt = "Example Excerpt",
            author = "Author Name",
            isPublic = 1,
            modified = "2023-06-18",
            imageURL = "/image.jpg",
            hasContent = true,
            hasArchive = true,
            hasEbook = true,
            tags = emptyList(),
            createArchive = true,
            createEbook = true
        )

        val bookmark = bookmarkEntity.toDomainModel()

        assertEquals(1, bookmark.id)
        assertEquals("http://example.com", bookmark.url)
        assertEquals("Example Title", bookmark.title)
        assertEquals("Example Excerpt", bookmark.excerpt)
        assertEquals("Author Name", bookmark.author)
        assertEquals(1, bookmark.public)
        assertEquals("2023-06-18", bookmark.modified)
        assertEquals("/image.jpg", bookmark.imageURL)
        assertEquals(true, bookmark.hasContent)
        assertEquals(true, bookmark.hasArchive)
        assertEquals(true, bookmark.hasEbook)
        assertEquals(0, bookmark.tags.size) // Ensure tags are empty
        assertEquals(true, bookmark.createArchive)
        assertEquals(true, bookmark.createEbook)
    }

    @Test
    fun `UpdateCachePayload toDTO maps correctly`() {
        val updateCachePayload = UpdateCachePayload(
            createArchive = true,
            createEbook = false,
            ids = listOf(1, 2, 3),
            keepMetadata = true,
            skipExist = false
        )

        val updateCachePayloadDTO = updateCachePayload.toDTO()

        assertEquals(true, updateCachePayloadDTO.createArchive)
        assertEquals(false, updateCachePayloadDTO.createEbook)
        assertEquals(listOf(1, 2, 3), updateCachePayloadDTO.ids)
        assertEquals(true, updateCachePayloadDTO.keepMetadata)
    }

    @Test
    fun `LivenessResponseDTO toDomainModel maps correctly`() {
        val releaseInfoDTO = ReleaseInfoDTO(
            version = "1.0.0",
            date = "2023-06-18",
            commit = "abc123"
        )

        val livenessResponseDTO = LivenessResponseDTO(
            ok = true,
            message = releaseInfoDTO
        )

        val livenessResponse = livenessResponseDTO.toDomainModel()

        assertEquals(true, livenessResponse.ok)
        assertEquals("1.0.0", livenessResponse.message?.version)
        assertEquals("2023-06-18", livenessResponse.message?.date)
        assertEquals("abc123", livenessResponse.message?.commit)
    }

    @Test
    fun `LivenessResponseDTO toDomainModel with null message maps correctly`() {
        val livenessResponseDTO = LivenessResponseDTO(
            ok = true,
            message = null
        )

        val livenessResponse = livenessResponseDTO.toDomainModel()

        assertEquals(true, livenessResponse.ok)
        assertEquals(null, livenessResponse.message)
    }

    @Test
    fun `LivenessResponseDTO toDomainModel with null ok maps correctly`() {
        val releaseInfoDTO = ReleaseInfoDTO(
            version = "1.0.0",
            date = "2023-06-18",
            commit = "abc123"
        )

        val livenessResponseDTO = LivenessResponseDTO(
            ok = null,
            message = releaseInfoDTO
        )

        val livenessResponse = livenessResponseDTO.toDomainModel()

        assertEquals(false, livenessResponse.ok)
        assertEquals("1.0.0", livenessResponse.message?.version)
        assertEquals("2023-06-18", livenessResponse.message?.date)
        assertEquals("abc123", livenessResponse.message?.commit)
    }

    @Test
    fun `ReleaseInfoDTO toDomainModel maps correctly`() {
        val releaseInfoDTO = ReleaseInfoDTO(
            version = "1.0.0",
            date = "2023-06-18",
            commit = "abc123"
        )

        val releaseInfo = releaseInfoDTO.toDomainModel()

        assertEquals("1.0.0", releaseInfo.version)
        assertEquals("2023-06-18", releaseInfo.date)
        assertEquals("abc123", releaseInfo.commit)
    }

    @Test
    fun `ReleaseInfoDTO toDomainModel with null fields maps correctly`() {
        val releaseInfoDTO = ReleaseInfoDTO(
            version = null,
            date = null,
            commit = null
        )

        val releaseInfo = releaseInfoDTO.toDomainModel()

        assertEquals("", releaseInfo.version) // Default value for version
        assertEquals("", releaseInfo.date) // Default value for date
        assertEquals("", releaseInfo.commit) // Default value for commit
    }

    @Test
    fun `LoginResponseDTO toProtoEntity maps correctly`() {
        val loginResponseMessageDTO = LoginResponseMessageDTO(
            expires = 3600,
            session = "session123",
            token = "token123"
        )

        val loginResponseDTO = LoginResponseDTO(
            ok = true,
            message = loginResponseMessageDTO,
            error = null
        )

        val userPreferences = loginResponseDTO.toProtoEntity(userName = "testUser")

        assertEquals("session123", userPreferences.session)
        assertEquals("testUser", userPreferences.username)
        assertEquals("token123", userPreferences.token)
        assertEquals(false, userPreferences.isLegacyApi)
    }

    @Test
    fun `LoginResponseDTO toProtoEntity with null message maps correctly`() {
        val loginResponseDTO = LoginResponseDTO(
            ok = true,
            message = null,
            error = null
        )

        val userPreferences = loginResponseDTO.toProtoEntity(userName = "testUser")

        assertEquals("", userPreferences.session) // Default value for session
        assertEquals("testUser", userPreferences.username)
        assertEquals("", userPreferences.token) // Default value for token
        assertEquals(false, userPreferences.isLegacyApi)
    }

    @Test
    fun `ReadableContentResponseDTO toDomainModel maps correctly`() {
        val readableMessageDto = ReadableMessageDto(
            content = "Sample Content",
            html = "<p>Sample HTML</p>"
        )

        val readableContentResponseDTO = ReadableContentResponseDTO(
            ok = true,
            message = readableMessageDto
        )

        val readableContent = readableContentResponseDTO.toDomainModel()

        assertEquals(true, readableContent.ok)
        assertEquals("Sample Content", readableContent.message.content)
        assertEquals("<p>Sample HTML</p>", readableContent.message.html)
    }

    @Test
    fun `ReadableContentResponseDTO toDomainModel with null fields maps correctly`() {
        val readableContentResponseDTO = ReadableContentResponseDTO(
            ok = null,
            message = null
        )

        val readableContent = readableContentResponseDTO.toDomainModel()

        assertEquals(false, readableContent.ok) // Default value for ok
        assertEquals("", readableContent.message.content) // Default value for content
        assertEquals("", readableContent.message.html) // Default value for html
    }

    @Test
    fun `ReadableMessageDto toDomainModel maps correctly`() {
        val readableMessageDto = ReadableMessageDto(
            content = "Sample Content",
            html = "<p>Sample HTML</p>"
        )

        val readableMessage = readableMessageDto.toDomainModel()

        assertEquals("Sample Content", readableMessage.content)
        assertEquals("<p>Sample HTML</p>", readableMessage.html)
    }

    @Test
    fun `ReadableMessageDto toDomainModel with null fields maps correctly`() {
        val readableMessageDto = ReadableMessageDto(
            content = null,
            html = null
        )

        val readableMessage = readableMessageDto.toDomainModel()

        assertEquals("", readableMessage.content) // Default value for content
        assertEquals("", readableMessage.html) // Default value for html
    }

}