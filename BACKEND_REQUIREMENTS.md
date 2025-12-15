# Backend Requirements for TravelMate Android App

This document lists all the backend endpoints and WebSocket events that need to be implemented for the features we've built in the Android app.

## 📋 Table of Contents
1. [Reservations API](#reservations-api)
2. [Favorites API](#favorites-api)
3. [Chat/WebSocket Events](#chatwebsocket-events)
4. [Additional Endpoints](#additional-endpoints)

---

## 1. Reservations API

### Endpoints Required:

#### 1.1 Create Reservation (User)
```
POST /api/reservations
Headers: Authorization: Bearer {token}
Body: {
  "pack_id": "string",
  "user_id": "string"  // from token
}
Response: Reservation object
```

#### 1.2 Get User Reservations
```
GET /api/reservations/user
Headers: Authorization: Bearer {token}
Response: List<Reservation> (with pack details populated)
```

#### 1.3 Get Agency Reservations
```
GET /api/reservations/agency
Headers: Authorization: Bearer {token}
Response: List<Reservation> (for all packs owned by the agency)
```

#### 1.4 Accept Reservation (Agency)
```
PUT /api/reservations/{reservationId}/accept
Headers: Authorization: Bearer {token}
Response: Reservation object (status: ACCEPTED)
```

#### 1.5 Reject Reservation (Agency)
```
PUT /api/reservations/{reservationId}/reject
Headers: Authorization: Bearer {token}
Response: Reservation object (status: REJECTED)
```

#### 1.6 Cancel Reservation (User)
```
PUT /api/reservations/{reservationId}/cancel
Headers: Authorization: Bearer {token}
Response: Reservation object (status: CANCELLED)
```

### Database Schema:
```typescript
Reservation {
  _id: ObjectId
  user_id: ObjectId (ref: User)
  user_name: String
  pack_id: ObjectId (ref: Pack)
  agency_id: ObjectId (ref: Agency)
  status: Enum ['pending', 'accepted', 'rejected', 'cancelled']
  created_at: Date
  updated_at: Date
}
```

---

## 2. Favorites API

### Endpoints Required:

#### 2.1 Add to Favorites
```
POST /api/favorites
Headers: Authorization: Bearer {token}
Body: {
  "pack_id": "string"
}
Response: { "success": true, "favorite_id": "string" }
```

#### 2.2 Remove from Favorites
```
DELETE /api/favorites/{packId}
Headers: Authorization: Bearer {token}
Response: { "success": true }
```

#### 2.3 Get User Favorites
```
GET /api/favorites
Headers: Authorization: Bearer {token}
Response: List<Pack> (all favorited packs)
```

#### 2.4 Check if Pack is Favorite
```
GET /api/favorites/{packId}
Headers: Authorization: Bearer {token}
Response: { "is_favorite": boolean }
```

### Database Schema:
```typescript
Favorite {
  _id: ObjectId
  user_id: ObjectId (ref: User)
  pack_id: ObjectId (ref: Pack)
  created_at: Date
}
// Index: unique(user_id, pack_id)
```

---

## 3. Chat/WebSocket Events

### Socket.IO Events Required:

#### 3.1 Join Conversation Room
**Event:** `join:conversation`
**Payload:** `{ "room_id": "string" }`
**Description:** User/agency joins a conversation room. Room ID format: `pack_{packId}_{agencyId}` or `agency_{agencyId}`

**Backend should:**
- Add user to the room
- Emit `conversation:joined` to confirm
- Optionally send conversation history

#### 3.2 Leave Conversation Room
**Event:** `leave:conversation`
**Payload:** `{ "room_id": "string" }`
**Description:** User/agency leaves a conversation room

#### 3.3 Send Message
**Event:** `message:send`
**Payload:** 
```json
{
  "sender_id": "string",
  "receiver_id": "string",
  "message": "string",
  "pack_id": "string" (optional),
  "timestamp": number
}
```
**Backend should:**
- Save message to database
- Broadcast to room: `message:received`
- Update conversation last message

#### 3.4 Receive Message
**Event:** `message:received`
**Payload:** 
```json
{
  "_id": "string",
  "sender_id": "string",
  "receiver_id": "string",
  "message": "string",
  "pack_id": "string" (optional),
  "timestamp": number,
  "read": boolean
}
```
**Description:** Broadcasted to all users in the conversation room

#### 3.5 Get Conversation History
**Event:** `conversation:history`
**Payload:** `{ "room_id": "string", "limit": number, "offset": number }`
**Response:** `conversation:history:response` with array of messages

### REST API Endpoints for Chat:

#### 3.6 Get Conversations List
```
GET /api/conversations
Headers: Authorization: Bearer {token}
Response: List<ChatConversation> {
  _id: string
  user_id: string
  user_name: string
  agency_id: string
  agency_name: string
  pack_id: string (optional)
  last_message: string
  last_message_time: number
  unread_count: number
}
```

#### 3.7 Get Conversation Messages
```
GET /api/conversations/{conversationId}/messages?limit=50&offset=0
Headers: Authorization: Bearer {token}
Response: List<ChatMessage>
```

#### 3.8 Mark Messages as Read
```
PUT /api/conversations/{conversationId}/read
Headers: Authorization: Bearer {token}
Response: { "success": true }
```

### Database Schema:
```typescript
ChatMessage {
  _id: ObjectId
  sender_id: ObjectId (ref: User/Agency)
  receiver_id: ObjectId (ref: User/Agency)
  message: String
  pack_id: ObjectId (ref: Pack, optional)
  timestamp: Date
  read: Boolean
  created_at: Date
}

Conversation {
  _id: ObjectId
  user_id: ObjectId (ref: User)
  agency_id: ObjectId (ref: Agency)
  pack_id: ObjectId (ref: Pack, optional)
  last_message: String
  last_message_time: Date
  unread_count_user: Number
  unread_count_agency: Number
  created_at: Date
  updated_at: Date
}
// Index: unique(user_id, agency_id, pack_id)
```

---

## 4. Additional Endpoints

### 4.1 Get Pack Details (Enhanced)
**Already exists but may need enhancement:**
```
GET /api/offers/{id}
```
**Should return:**
- Full pack details
- Agency information
- Available places count
- Related packs (optional)

### 4.2 Notification System (Optional but Recommended)
```
GET /api/notifications
Headers: Authorization: Bearer {token}
Response: List<Notification> {
  _id: string
  user_id: string
  type: 'reservation_accepted' | 'reservation_rejected' | 'new_message' | 'pack_updated'
  title: string
  message: string
  data: object (pack_id, reservation_id, etc.)
  read: boolean
  created_at: number
}
```

**WebSocket Event:**
- `notification:new` - Emit when new notification is created

---

## 5. Implementation Priority

### High Priority (Required for basic functionality):
1. ✅ **Reservations API** - All CRUD operations
2. ✅ **Favorites API** - Add/remove/get favorites
3. ✅ **Chat WebSocket** - Basic messaging (join, send, receive)

### Medium Priority (Enhanced UX):
4. ⚠️ **Conversation History** - Load previous messages
5. ⚠️ **Unread Count** - Track unread messages
6. ⚠️ **Notifications** - Reservation status updates

### Low Priority (Nice to have):
7. 📝 **Typing Indicators** - Show when user is typing
8. 📝 **Message Status** - Delivered, read receipts
9. 📝 **File Attachments** - Send images/files in chat

---

## 6. Authentication & Authorization

All endpoints should:
- Require JWT token in `Authorization: Bearer {token}` header
- Validate user permissions:
  - **Reservations**: Users can create/cancel, Agencies can accept/reject their own packs
  - **Favorites**: Users can manage their own favorites
  - **Chat**: Users can chat with agencies, Agencies can chat with users

---

## 7. Error Handling

All endpoints should return consistent error format:
```json
{
  "error": true,
  "message": "Error description",
  "code": "ERROR_CODE"
}
```

Common error codes:
- `UNAUTHORIZED` - Missing/invalid token
- `FORBIDDEN` - User doesn't have permission
- `NOT_FOUND` - Resource doesn't exist
- `VALIDATION_ERROR` - Invalid input data
- `SERVER_ERROR` - Internal server error

---

## 8. Testing Checklist

Before deploying, ensure:
- [ ] All reservation endpoints work (create, get, accept, reject)
- [ ] Favorites endpoints work (add, remove, get list)
- [ ] WebSocket connection works
- [ ] Messages are saved to database
- [ ] Messages are broadcasted correctly
- [ ] Room joining/leaving works
- [ ] Authentication is enforced on all endpoints
- [ ] Error handling is consistent
- [ ] Database indexes are created for performance

---

## Notes for Backend Team

1. **Room ID Format**: Use `pack_{packId}_{agencyId}` for pack-specific chats, `agency_{agencyId}` for general agency chats
2. **Real-time Updates**: Use Socket.IO for chat, consider adding Socket.IO for reservation status updates too
3. **Database Indexes**: Add indexes on frequently queried fields (user_id, pack_id, agency_id, status)
4. **Pagination**: Implement pagination for messages and reservations lists
5. **Rate Limiting**: Consider rate limiting for chat messages to prevent spam

---

**Last Updated:** Based on Android app implementation as of current date
**Contact:** Check Android app code for exact data models and expected response formats

