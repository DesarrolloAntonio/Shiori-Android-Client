package com.desarrollodroide.data.mapper

import com.desarrollodroide.data.UserPreferences
import com.desarrollodroide.data.local.room.entity.BookmarkEntity
import com.desarrollodroide.data.local.room.entity.TagEntity
import com.desarrollodroide.model.*
import com.desarrollodroide.network.model.*

fun SessionDTO.toDomainModel() = User(
    token = token?:"",
    session = session?:"",
    account = account?.toDomainModel()?:Account()
)

fun AccountDTO.toDomainModel() = Account(
    id = -1,
    userName = userName?:"",
    password = password?:"",
    owner = isOwner?:false,
    serverUrl = "",
    isLegacyApi = isLegacyApi?:false
)

fun SessionDTO.toProtoEntity(): UserPreferences = UserPreferences.newBuilder()
    .setSession(session?:"")
    .setUsername(account?.userName?:"")
    .setId(account?.id?:-1)
    .setOwner(account?.isOwner?:false)
    .setIsLegacyApi(true)
    .build()

fun BookmarkDTO.toDomainModel(serverUrl: String = "") = Bookmark(
    id = id?:0,
    url = url?:"",
    title = title?:"",
    excerpt = excerpt?:"",
    author = author?:"",
    public = public?:0,
    modified = modified?:"",
    imageURL = "$serverUrl$imageURL",
    hasContent = hasContent?:false,
    hasArchive = hasArchive?:false,
    hasEbook = hasEbook?:false,
    tags = tags?.map { it.toDomainModel() }?: emptyList(),
    createArchive = createArchive?:false,
    createEbook = createEbook?:false
)

fun BookmarksDTO.toDomainModel(serverUrl: String) = Bookmarks(
    error = "",
    page = page?:0,
    maxPage = maxPage?:0,
    bookmarks = bookmarks?.map { it.toDomainModel(serverUrl) }?: emptyList()
)

fun TagDTO.toDomainModel() = Tag(
    id = id?:0,
    name = name?:"",
    selected = false,
    nBookmarks = nBookmarks?:0
)

fun TagDTO.toEntityModel() = TagEntity(
    id = id?:0,
    name = name?:"",
    nBookmarks = nBookmarks?:0
)

fun TagEntity.toDomainModel() = Tag(
    id = id,
    name = name,
    selected = false,
    nBookmarks = nBookmarks
)

fun Account.toRequestBody() =
    LoginRequestPayload(
        username = userName,
        password = password
    )

fun BookmarkDTO.toEntityModel() = BookmarkEntity(
    id = id?:0,
    url = url?:"",
    title = title?:"",
    excerpt = excerpt?:"",
    author = author?:"",
    isPublic = public?:0,
    modified = modified?:"",
    imageURL = imageURL?:"",
    hasContent = hasContent?:false,
    hasArchive = hasArchive?:false,
    hasEbook = hasEbook?:false,
    tags = tags?.map { it.toDomainModel() } ?: emptyList(),
    createArchive = createArchive?:false,
    createEbook = createEbook?:false
)

fun BookmarkEntity.toDomainModel() = Bookmark(
    id = id,
    url = url,
    title = title,
    excerpt = excerpt,
    author = author,
    public = isPublic,
    modified = modified,
    imageURL = imageURL,
    hasContent = hasContent,
    hasArchive = hasArchive,
    hasEbook = hasEbook,
    tags = tags,
    createArchive = createArchive,
    createEbook = createEbook
)

fun UpdateCachePayload.toDTO() = UpdateCachePayloadDTO(
    createArchive = createArchive,
    createEbook = createEbook,
    ids = ids,
    keepMetadata = keepMetadata,
)

fun UpdateCachePayload.toV1DTO() = UpdateCachePayloadV1DTO(
    createArchive = createArchive,
    createEbook = createEbook,
    ids = ids,
    keepMetadata = keepMetadata,
    skipExist = skipExist
)

fun LivenessResponseDTO.toDomainModel() = LivenessResponse(
    ok = ok?:false,
    message = message?.toDomainModel()
)

fun ReleaseInfoDTO.toDomainModel() = ReleaseInfo(
    version = version?:"",
    date = date?:"",
    commit = commit?:""
)


fun LoginResponseDTO.toProtoEntity(
    userName: String,
): UserPreferences = UserPreferences.newBuilder()
    .setSession(message?.session?:"")
    .setUsername(userName)
    .setToken(message?.token?:"")
    .setIsLegacyApi(false)
    .build()

fun LoginResponseMessageDTO.toDomainModel() = LoginResponseMessage(
    expires = expires?:0,
    session = session?:"",
    token = token?:""
)

fun ReadableContentResponseDTO.toDomainModel() = ReadableContent(
    ok = ok?:false,
    message = message?.toDomainModel() ?: ReadableMessage("", "")
)

fun ReadableMessageDto.toDomainModel() = ReadableMessage(
    content = content?:"",
    html = html?:""
)


