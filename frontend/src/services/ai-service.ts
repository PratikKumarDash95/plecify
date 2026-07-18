import { apiClient } from "@/lib/api-client";
import { unwrap } from "@/lib/api-helpers";
import type { ApiResponse } from "@/types/api";
import type {
  ChatRequest,
  ChatResponse,
  TextAssistRequest,
  TextAssistResponse,
} from "@/types/ai";

/** AI assistant endpoints under /api/v1/ai. */
export const aiService = {
  async assist(payload: TextAssistRequest): Promise<TextAssistResponse> {
    const { data } = await apiClient.post<ApiResponse<TextAssistResponse>>("/ai/assist", payload);
    return unwrap(data);
  },

  async chat(payload: ChatRequest): Promise<ChatResponse> {
    const { data } = await apiClient.post<ApiResponse<ChatResponse>>("/ai/chat", payload);
    return unwrap(data);
  },
};
