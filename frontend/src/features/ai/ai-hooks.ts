import { useMutation } from "@tanstack/react-query";
import { aiService } from "@/services/ai-service";
import type { ChatRequest, TextAssistRequest } from "@/types/ai";

/** Inline text transformation (polish/summarize/rewrite). */
export function useTextAssist() {
  return useMutation({
    mutationFn: (payload: TextAssistRequest) => aiService.assist(payload),
  });
}

/** Chatbot conversation turn. */
export function useAiChat() {
  return useMutation({
    mutationFn: (payload: ChatRequest) => aiService.chat(payload),
  });
}
