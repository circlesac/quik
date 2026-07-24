package dev.octoshrimpy.quik.data.db

import dev.octoshrimpy.quik.data.db.entity.BlockedNumberEntity
import dev.octoshrimpy.quik.data.db.entity.ContactEntity
import dev.octoshrimpy.quik.data.db.entity.ContactGroupContactEntity
import dev.octoshrimpy.quik.data.db.entity.ContactGroupEntity
import dev.octoshrimpy.quik.data.db.entity.ConversationEntity
import dev.octoshrimpy.quik.data.db.entity.ConversationRecipientEntity
import dev.octoshrimpy.quik.data.db.entity.EmojiReactionEntity
import dev.octoshrimpy.quik.data.db.entity.MessageContentFilterEntity
import dev.octoshrimpy.quik.data.db.entity.MessageEntity
import dev.octoshrimpy.quik.data.db.entity.MmsPartEntity
import dev.octoshrimpy.quik.data.db.entity.PhoneNumberEntity
import dev.octoshrimpy.quik.data.db.entity.RecipientEntity
import dev.octoshrimpy.quik.data.db.entity.ScheduledMessageEntity
import dev.octoshrimpy.quik.data.db.relation.ContactGroupWithContacts
import dev.octoshrimpy.quik.data.db.relation.ContactWithNumbers
import dev.octoshrimpy.quik.data.db.relation.ConversationWithRelations
import dev.octoshrimpy.quik.data.db.relation.MessageWithRelations
import dev.octoshrimpy.quik.data.db.relation.RecipientWithContact
import dev.octoshrimpy.quik.model.BlockedNumber
import dev.octoshrimpy.quik.model.Contact
import dev.octoshrimpy.quik.model.ContactGroup
import dev.octoshrimpy.quik.model.Conversation
import dev.octoshrimpy.quik.model.EmojiReaction
import dev.octoshrimpy.quik.model.Message
import dev.octoshrimpy.quik.model.MessageContentFilter
import dev.octoshrimpy.quik.model.MmsPart
import dev.octoshrimpy.quik.model.PhoneNumber
import dev.octoshrimpy.quik.model.Recipient
import dev.octoshrimpy.quik.model.ScheduledMessage

// ---------------------------------------------------------------------------
// READ: Entity / Relation -> Domain model
// ---------------------------------------------------------------------------

fun MessageWithRelations.toModel(): Message = Message().apply {
    id = message.id
    threadId = message.threadId
    contentId = message.contentId
    address = message.address
    boxId = message.boxId
    type = message.type
    date = message.date
    dateSent = message.dateSent
    seen = message.seen
    read = message.read
    locked = message.locked
    subId = message.subId
    body = message.body
    errorCode = message.errorCode
    deliveryStatus = message.deliveryStatus
    // attachmentType is computed over attachmentTypeString, so set the string only
    attachmentTypeString = message.attachmentTypeString
    mmsDeliveryStatusString = message.mmsDeliveryStatusString
    readReportString = message.readReportString
    errorType = message.errorType
    messageSize = message.messageSize
    messageType = message.messageType
    mmsStatus = message.mmsStatus
    subject = message.subject
    textContentType = message.textContentType
    isEmojiReaction = message.isEmojiReaction
    sendAsGroup = message.sendAsGroup
    parts = this@toModel.parts.map { it.toModel() }.toMutableList()
    // emojiReactions is a val MutableList in the model, so mutate in place
    emojiReactions.addAll(this@toModel.emojiReactions.map { it.toModel() })
}

fun MmsPartEntity.toModel(): MmsPart = MmsPart().apply {
    id = this@toModel.id
    messageId = this@toModel.messageId
    type = this@toModel.type
    seq = this@toModel.seq
    name = this@toModel.name
    text = this@toModel.text
}

fun EmojiReactionEntity.toModel(): EmojiReaction = EmojiReaction().apply {
    id = this@toModel.id
    reactionMessageId = this@toModel.reactionMessageId
    senderAddress = this@toModel.senderAddress
    emoji = this@toModel.emoji
    originalMessageText = this@toModel.originalMessageText
    threadId = this@toModel.threadId
    // targetMessageId has no field in the domain model (join key only)
}

fun ContactWithNumbers.toModel(): Contact = Contact(
    lookupKey = contact.lookupKey,
    numbers = numbers.map { it.toModel() }.toMutableList(),
    name = contact.name,
    photoUri = contact.photoUri,
    starred = contact.starred,
    lastUpdate = contact.lastUpdate
)

fun PhoneNumberEntity.toModel(): PhoneNumber = PhoneNumber(
    id = id,
    accountType = accountType,
    address = address,
    type = type,
    isDefault = isDefault
    // contactLookupKey has no field in the domain model (ownership FK only)
)

fun RecipientWithContact.toModel(): Recipient = Recipient(
    id = recipient.id,
    address = recipient.address,
    contact = contact?.toModel(),
    lastUpdate = recipient.lastUpdate
)

fun ConversationWithRelations.toModel(): Conversation = Conversation(
    id = conversation.id,
    archived = conversation.archived,
    blocked = conversation.blocked,
    pinned = conversation.pinned,
    recipients = recipients.map { it.toModel() }.toMutableList(),
    lastMessage = lastMessage?.toModel(),
    draft = conversation.draft,
    draftDate = conversation.draftDate,
    blockingClient = conversation.blockingClient,
    blockReason = conversation.blockReason,
    name = conversation.name,
    sendAsGroup = conversation.sendAsGroup
)

fun ContactGroupWithContacts.toModel(): ContactGroup = ContactGroup(
    id = group.id,
    title = group.title,
    contacts = contacts.map { it.toModel() }.toMutableList()
)

fun ScheduledMessageEntity.toModel(): ScheduledMessage = ScheduledMessage(
    id = id,
    date = date,
    subId = subId,
    recipients = recipients.toMutableList(),
    sendAsGroup = sendAsGroup,
    body = body,
    attachments = attachments.toMutableList(),
    conversationId = conversationId
)

fun BlockedNumberEntity.toModel(): BlockedNumber = BlockedNumber(
    id = id,
    address = address
)

fun MessageContentFilterEntity.toModel(): MessageContentFilter = MessageContentFilter(
    id = id,
    value = value,
    caseSensitive = caseSensitive,
    isRegex = isRegex,
    includeContacts = includeContacts
)

// ---------------------------------------------------------------------------
// WRITE: Domain model -> Entity (for insert / update)
// ---------------------------------------------------------------------------

fun Message.toEntity(): MessageEntity = MessageEntity(
    id = id,
    threadId = threadId,
    contentId = contentId,
    address = address,
    boxId = boxId,
    type = type,
    date = date,
    dateSent = dateSent,
    seen = seen,
    read = read,
    locked = locked,
    subId = subId,
    body = body,
    errorCode = errorCode,
    deliveryStatus = deliveryStatus,
    attachmentTypeString = attachmentTypeString,
    mmsDeliveryStatusString = mmsDeliveryStatusString,
    readReportString = readReportString,
    errorType = errorType,
    messageSize = messageSize,
    messageType = messageType,
    mmsStatus = mmsStatus,
    subject = subject,
    textContentType = textContentType,
    isEmojiReaction = isEmojiReaction,
    sendAsGroup = sendAsGroup
)

// MmsPart.messageId references Message.contentId (not Message.id) per sync/migration
fun Message.partEntities(): List<MmsPartEntity> = parts.map { it.toEntity(contentId) }

fun Message.emojiReactionEntities(): List<EmojiReactionEntity> =
    emojiReactions.map { it.toEntity(id) }

fun MmsPart.toEntity(messageId: Long): MmsPartEntity = MmsPartEntity(
    id = id,
    messageId = messageId,
    type = type,
    seq = seq,
    name = name,
    text = text
)

fun EmojiReaction.toEntity(targetMessageId: Long): EmojiReactionEntity = EmojiReactionEntity(
    id = id,
    reactionMessageId = reactionMessageId,
    senderAddress = senderAddress,
    emoji = emoji,
    originalMessageText = originalMessageText,
    threadId = threadId,
    targetMessageId = targetMessageId
)

fun Conversation.toEntity(): ConversationEntity = ConversationEntity(
    id = id,
    archived = archived,
    blocked = blocked,
    pinned = pinned,
    draft = draft,
    draftDate = draftDate,
    blockingClient = blockingClient,
    blockReason = blockReason,
    name = name,
    sendAsGroup = sendAsGroup,
    lastMessageId = lastMessage?.id
)

fun Conversation.recipientJunctions(): List<ConversationRecipientEntity> =
    recipients.map { ConversationRecipientEntity(conversationId = id, recipientId = it.id) }

fun Recipient.toEntity(): RecipientEntity = RecipientEntity(
    id = id,
    address = address,
    contactLookupKey = contact?.lookupKey,
    lastUpdate = lastUpdate
)

fun Contact.toEntity(): ContactEntity = ContactEntity(
    lookupKey = lookupKey,
    name = name,
    photoUri = photoUri,
    starred = starred,
    lastUpdate = lastUpdate
)

fun Contact.numberEntities(): List<PhoneNumberEntity> = numbers.map { it.toEntity(lookupKey) }

fun PhoneNumber.toEntity(contactLookupKey: String): PhoneNumberEntity = PhoneNumberEntity(
    id = id,
    contactLookupKey = contactLookupKey,
    accountType = accountType,
    address = address,
    type = type,
    isDefault = isDefault
)

fun ContactGroup.toEntity(): ContactGroupEntity = ContactGroupEntity(
    id = id,
    title = title
)

fun ContactGroup.contactJunctions(): List<ContactGroupContactEntity> =
    contacts.map { ContactGroupContactEntity(groupId = id, contactLookupKey = it.lookupKey) }

fun ScheduledMessage.toEntity(): ScheduledMessageEntity = ScheduledMessageEntity(
    id = id,
    date = date,
    subId = subId,
    recipients = recipients,
    sendAsGroup = sendAsGroup,
    body = body,
    attachments = attachments,
    conversationId = conversationId
)

fun BlockedNumber.toEntity(): BlockedNumberEntity = BlockedNumberEntity(
    id = id,
    address = address
)

fun MessageContentFilter.toEntity(): MessageContentFilterEntity = MessageContentFilterEntity(
    id = id,
    value = value,
    caseSensitive = caseSensitive,
    isRegex = isRegex,
    includeContacts = includeContacts
)
