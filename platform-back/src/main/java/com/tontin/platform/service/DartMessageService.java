package com.tontin.platform.service;

import com.tontin.platform.dto.dart.request.CreateMessageRequest;
import com.tontin.platform.dto.dart.response.MessageResponse;
import com.tontin.platform.dto.dart.response.PageResponse;
import java.util.UUID;

/**
 * Service for dart chat messages.
 * Only members of a dart can read and send messages.
 */
public interface DartMessageService {

    /**
     * Get paginated messages for a dart. Caller must be a member of the dart.
     *
     * @param dartId dart id
     * @param page   page number (0-based)
     * @param size   page size
     * @return paginated messages
     */
    PageResponse<MessageResponse> getMessages(UUID dartId, int page, int size);

    /**
     * Create a message in a dart. Caller must be an active member of the dart.
     *
     * @param dartId  dart id
     * @param request message content and optional GIF URL
     * @return the created message
     */
    MessageResponse createMessage(UUID dartId, CreateMessageRequest request);

    /**
     * Add a reaction (emoji) to a message. Idempotent if same member adds same emoji again.
     *
     * @param dartId    dart id
     * @param messageId message id
     * @param emoji     emoji string (e.g. "👍", "❤️")
     */
    void addReaction(UUID dartId, UUID messageId, String emoji);

    /**
     * Remove a reaction from a message.
     *
     * @param dartId    dart id
     * @param messageId message id
     * @param emoji     emoji to remove
     */
    void removeReaction(UUID dartId, UUID messageId, String emoji);
}
