package io.streamlayer.react.module.internal

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReadableMap
import io.streamlayer.sdk.SLRInviteData

internal data class StreamLayerInvite(
  val linkId: String?,
  val eventId: String?,
  val externalEventId: String?,
  val groupId: String?,
  val externalGroupId: String?,
  val gamification: Boolean?,
  val groupType: GroupType?,
  val user: User?
) {

  data class User(
    val id: String?,
    val tinodeUserId: String?,
    val name: String?,
    val username: String?,
    val avatar: String?
  )

  enum class GroupType {
    Chat,
    WatchParty
  }

  fun toMap(): ReadableMap = Arguments.createMap().apply {
    linkId?.let { putString(PROP_LINK_ID, it) }
    eventId?.let { putString(PROP_EVENT_ID, it) }
    externalEventId?.let { putString(PROP_EXTERNAL_EVENT_ID, it) }
    groupId?.let { putString(PROP_GROUP_ID, it) }
    externalGroupId?.let { putString(PROP_EXTERNAL_GROUP_ID, it) }
    gamification?.let { putBoolean(PROP_GAMIFICATION, it) }
    groupType?.let { putString(PROP_GROUP_TYPE, it.name) }
    user?.let {
      putMap(PROP_USER, Arguments.createMap().apply {
        putString(PROP_ID, it.id)
        putString(PROP_TINODE_USER_ID, it.tinodeUserId)
        putString(PROP_NAME, it.name)
        putString(PROP_USERNAME, it.username)
        putString(PROP_AVATAR, it.avatar)
      })
    }
  }

  companion object {
    private const val PROP_LINK_ID = "linkId"
    private const val PROP_EVENT_ID = "eventId"
    private const val PROP_EXTERNAL_EVENT_ID = "externalEventId"
    private const val PROP_GROUP_ID = "groupId"
    private const val PROP_EXTERNAL_GROUP_ID = "externalGroupId"
    private const val PROP_GAMIFICATION = "gamification"
    private const val PROP_GROUP_TYPE = "groupType"
    private const val PROP_USER = "user"
    private const val PROP_ID = "id"
    private const val PROP_TINODE_USER_ID = "tinodeUserId"
    private const val PROP_NAME = "name"
    private const val PROP_USERNAME = "username"
    private const val PROP_AVATAR = "avatar"

    fun fromMap(map: ReadableMap): StreamLayerInvite {
      var linkId: String? = null
      if (map.hasKey(PROP_LINK_ID)) {
        linkId = map.getString(PROP_LINK_ID)
      }
      var eventId: String? = null
      if (map.hasKey(PROP_EVENT_ID)) {
        eventId = map.getString(PROP_EVENT_ID)
      }
      var externalEventId: String? = null
      if (map.hasKey(PROP_EXTERNAL_EVENT_ID)) {
        externalEventId = map.getString(PROP_EXTERNAL_EVENT_ID)
      }
      var groupId: String? = null
      if (map.hasKey(PROP_GROUP_ID)) {
        groupId = map.getString(PROP_GROUP_ID)
      }
      var externalGroupId: String? = null
      if (map.hasKey(PROP_EXTERNAL_GROUP_ID)) {
        externalGroupId = map.getString(PROP_EXTERNAL_GROUP_ID)
      }
      var gamification: Boolean? = null
      if (map.hasKey(PROP_GAMIFICATION)) {
        gamification = map.getBoolean(PROP_GAMIFICATION)
      }
      var groupType: GroupType? = null
      if (map.hasKey(PROP_GROUP_TYPE)) {
        groupType = getGroupType(map.getString(PROP_GROUP_TYPE))
      }

      var user: User? = null
      if (map.hasKey(PROP_USER)) map.getMap(PROP_USER)?.let { userMap ->

        var userId: String? = null
        if (userMap.hasKey(PROP_ID)) {
          userId = userMap.getString(PROP_ID)
        }

        var tinodeUserId: String? = null
        if (userMap.hasKey(PROP_TINODE_USER_ID)) {
          tinodeUserId = userMap.getString(PROP_TINODE_USER_ID)
        }

        var name: String? = null
        if (userMap.hasKey(PROP_NAME)) {
          name = userMap.getString(PROP_NAME)
        }

        var username: String? = null
        if (userMap.hasKey(PROP_USERNAME)) {
          username = userMap.getString(PROP_USERNAME)
        }

        var avatar: String? = null
        if (userMap.hasKey(PROP_AVATAR)) {
          avatar = userMap.getString(PROP_AVATAR)
        }

        user = User(
          id = userId,
          tinodeUserId = tinodeUserId,
          name = name,
          username = username,
          avatar = avatar
        )
      }

      return StreamLayerInvite(
        linkId = linkId,
        eventId = eventId,
        externalEventId = externalEventId,
        groupId = groupId,
        externalGroupId = externalGroupId,
        gamification = gamification,
        groupType = groupType,
        user = user
      )
    }

    private fun getGroupType(type: String?) = when (type) {
      "WatchParty" -> GroupType.WatchParty
      "Chat" -> GroupType.Chat
      else -> null
    }
  }

}

internal fun SLRInviteData?.toDomain(): StreamLayerInvite? {
  return if (this == null) null else StreamLayerInvite(
    linkId = linkId,
    eventId = eventId,
    externalEventId = externalEventId,
    groupId = groupId,
    externalGroupId = groupId,
    gamification = gamification,
    groupType = when (groupType) {
      SLRInviteData.GroupType.WATCH_PARTY -> StreamLayerInvite.GroupType.WatchParty
      SLRInviteData.GroupType.CHAT -> StreamLayerInvite.GroupType.Chat
      else -> null
    },
    user = StreamLayerInvite.User(
      id = user.id,
      tinodeUserId = user.tinodeUserId,
      name = user.name,
      username = user.username,
      avatar = user.avatar
    )
  )
}

internal fun StreamLayerInvite?.toData(): SLRInviteData? {
  return if (this == null || user?.id.isNullOrEmpty()) null else SLRInviteData(
    linkId = linkId,
    eventId = eventId,
    externalEventId = externalEventId,
    groupId = groupId,
    externalGroupId = groupId,
    gamification = gamification,
    groupType = when (groupType) {
      StreamLayerInvite.GroupType.WatchParty -> SLRInviteData.GroupType.WATCH_PARTY
      StreamLayerInvite.GroupType.Chat -> SLRInviteData.GroupType.CHAT
      else -> null
    },
    user = SLRInviteData.User(
      id = user?.id!!,
      tinodeUserId = user.tinodeUserId,
      name = user.name,
      username = user.username,
      avatar = user.avatar
    )
  )
}
