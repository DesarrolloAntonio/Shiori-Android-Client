package com.desarrollodroide.data.mapper

import com.desarrollodroide.data.local.room.entity.BookmarkEntity
import com.desarrollodroide.data.local.room.entity.TagEntity
import com.desarrollodroide.model.Account
import com.desarrollodroide.model.Bookmark
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
        )

        val account = accountDTO.toDomainModel()

        assertEquals("testUser", account.userName)
        assertEquals("password", account.password)
        assertEquals(true, account.owner)
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
            createdAt = "2023-06-19",
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
        assertEquals("2023-06-19", bookmark.createAt)
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
            createdAt = "2023-06-19",
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
        assertEquals("2023-06-19", bookmark.createAt)
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
            createdAt = "2023-06-19",
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
        assertEquals("2023-06-19", bookmarkEntity.createdAt)
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
            createdAt = null,
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
        assertEquals("", bookmarkEntity.createdAt) // Default value for createdAt
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
            createdAt = "2023-06-19",
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
        assertEquals("2023-06-19", bookmark.createAt)
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
            createdAt = "2023-06-19",
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
        assertEquals("2023-06-19", bookmark.createAt)
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

    @Test
    fun `toAddBookmarkDTO should map fields correctly`() {
        // Given
        val tags = listOf(Tag(id = 1, name = "education"), Tag(id = 2, name = "reading"))
        val bookmark = Bookmark(
            url = "https://example.com",
            tags = tags,
            public = 1,
            createArchive = true,
            createEbook = true,
            title = "Example Title"
        )

        // When
        val dto = bookmark.toAddBookmarkDTO()

        // Then
        assertNull(dto.id)
        assertEquals("https://example.com", dto.url)
        assertEquals("Example Title", dto.title)
        assertEquals("", dto.excerpt)
        assertNull(dto.author)
        assertEquals(1, dto.public)
        assertNull(dto.createdAt)
        assertNull(dto.modified)
        assertNull(dto.imageURL)
        assertNull(dto.hasContent)
        assertNull(dto.hasArchive)
        assertNull(dto.hasEbook)
        assertEquals(2, dto.tags?.size)
        assertEquals("education", dto.tags?.get(0)?.name)
        assertEquals("reading", dto.tags?.get(1)?.name)
        assertTrue(dto.createArchive == true)
        assertTrue(dto.createEbook == true)
    }

    @Test
    fun `toEditBookmarkDTO should map all fields correctly`() {
        // Given
        val tags = listOf(Tag(id = 1, name = "education"), Tag(id = 2, name = "reading"))
        val bookmark = Bookmark(
            id = 1,
            url = "https://example.com",
            title = "Example Title",
            excerpt = "An example excerpt",
            author = "Author Name",
            public = 1,
            createAt = "2023-01-01T12:00:00",
            modified = "2023-01-01T12:00:00",
            imageURL = "https://example.com/image.jpg",
            hasContent = true,
            hasArchive = false,
            hasEbook = false,
            tags = tags,
            createArchive = true,
            createEbook = false
        )

        // When
        val dto = bookmark.toEditBookmarkDTO()

        // Then
        assertEquals(1, dto.id)
        assertEquals("https://example.com", dto.url)
        assertEquals("Example Title", dto.title)
        assertEquals("An example excerpt", dto.excerpt)
        assertEquals("Author Name", dto.author)
        assertEquals(1, dto.public)
        assertEquals("2023-01-01T12:00:00", dto.createdAt)
        assertEquals("2023-01-01T12:00:00", dto.modified)
        assertEquals("https://example.com/image.jpg", dto.imageURL)
        assertTrue(dto.hasContent == true)
        assertFalse(dto.hasArchive == true)
        assertFalse(dto.hasEbook == true)
        assertEquals(2, dto.tags?.size)
        assertEquals("education", dto.tags?.get(0)?.name)
        assertEquals("reading", dto.tags?.get(1)?.name)
        assertTrue(dto.createArchive == true)
        assertFalse(dto.createEbook == true)
    }

    @Test
    fun `toEditBookmarkJson should include all fields except hasEbook and createEbook`() {
        // Given
        val tags = listOf(TagDTO(id = 1, name = "education", nBookmarks = null), TagDTO(id = 2, name = "reading", nBookmarks = null))
        val bookmarkDTO = BookmarkDTO(
            id = 1,
            url = "https://example.com",
            title = "Example Title",
            excerpt = "An example excerpt",
            author = "Author Name",
            public = 1,
            createdAt = "2023-01-01T12:00:00",
            modified = "2023-01-01T12:00:00",
            imageURL = "https://example.com/image.jpg",
            hasContent = true,
            hasArchive = false,
            hasEbook = true,
            tags = tags,
            createArchive = true,
            createEbook = true
        )

        // When
        val json = bookmarkDTO.toEditBookmarkJson()

        // Then
        assertTrue(json.contains("\"id\":1"))
        assertTrue(json.contains("\"url\":\"https://example.com\""))
        assertTrue(json.contains("\"title\":\"Example Title\""))
        assertTrue(json.contains("\"excerpt\":\"An example excerpt\""))
        assertTrue(json.contains("\"author\":\"Author Name\""))
        assertTrue(json.contains("\"public\":1"))
        assertTrue(json.contains("\"createdAt\":\"2023-01-01T12:00:00\""))
        assertTrue(json.contains("\"modified\":\"2023-01-01T12:00:00\""))
        assertTrue(json.contains("\"imageURL\":\"https://example.com/image.jpg\""))
        assertTrue(json.contains("\"hasContent\":true"))
        assertTrue(json.contains("\"hasArchive\":false"))
        assertTrue(json.contains("\"tags\":[{\"name\":\"education\"},{\"name\":\"reading\"}]"))
        assertTrue(json.contains("\"create_archive\":true"))
        assertFalse(json.contains("\"hasEbook\":true")) // Excluded in JSON
        assertTrue(json.contains("\"create_archive\":true"))
    }

}