// AI assistant domain types, mirroring com.campusconnect.portal.dto.ai.*

/** Inline text transformations offered on free-text fields. */
export type AssistAction =
  | "POLISH"
  | "SUMMARIZE"
  | "REWRITE_FORMAL"
  | "REWRITE_CASUAL"
  | "EXPAND";

export interface TextAssistRequest {
  action: AssistAction;
  text: string;
  /** Optional hint about what the text is, e.g. "job description", "cover letter". */
  context?: string;
}

export interface TextAssistResponse {
  result: string;
}

/** A single chatbot turn. Role is "user" or "assistant". */
export interface ChatMessageDto {
  role: "user" | "assistant";
  content: string;
}

export interface ChatRequest {
  messages: ChatMessageDto[];
}

export interface ChatResponse {
  reply: string;
}
