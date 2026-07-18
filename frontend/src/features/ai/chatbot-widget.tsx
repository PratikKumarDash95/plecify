import { useEffect, useRef, useState } from "react";
import { Icon } from "@/components/ui/icon";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";
import { toApiError } from "@/lib/api-helpers";
import { useAiChat } from "./ai-hooks";
import type { ChatMessageDto } from "@/types/ai";

const GREETING: ChatMessageDto = {
  role: "assistant",
  content:
    "Hi! I'm the PlacementPro assistant. Ask me about jobs, eligibility, or applications, " +
    "or paste a draft and I'll help you polish it.",
};

/**
 * Floating chatbot launcher + slide-over panel, mounted once in the dashboard layout so every
 * role gets an assistant. Keeps the recent conversation in local state and sends the last few
 * turns to the backend on each message.
 */
export function ChatbotWidget() {
  const [open, setOpen] = useState(false);
  const [messages, setMessages] = useState<ChatMessageDto[]>([GREETING]);
  const [input, setInput] = useState("");
  const chat = useAiChat();
  const scrollRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (open) scrollRef.current?.scrollTo({ top: scrollRef.current.scrollHeight, behavior: "smooth" });
  }, [messages, open]);

  const send = async () => {
    const text = input.trim();
    if (!text || chat.isPending) return;

    const nextMessages = [...messages, { role: "user", content: text } as ChatMessageDto];
    setMessages(nextMessages);
    setInput("");

    try {
      // Send the last 20 turns (matching the backend cap), skipping the local greeting.
      const history = nextMessages.filter((m) => m !== GREETING).slice(-20);
      const { reply } = await chat.mutateAsync({ messages: history });
      setMessages((prev) => [...prev, { role: "assistant", content: reply }]);
    } catch (err) {
      const apiErr = toApiError(err);
      setMessages((prev) => [
        ...prev,
        {
          role: "assistant",
          content:
            apiErr.status === 503
              ? "AI isn't configured yet. An OpenRouter API key and model need to be set on the server."
              : apiErr.message || "Sorry, something went wrong. Please try again.",
        },
      ]);
    }
  };

  const onKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      void send();
    }
  };

  return (
    <>
      {/* Launcher */}
      <button
        type="button"
        onClick={() => setOpen((v) => !v)}
        aria-label={open ? "Close assistant" : "Open assistant"}
        className={cn(
          "fixed bottom-5 right-5 z-40 flex h-14 w-14 items-center justify-center rounded-full",
          "bg-primary-container text-white shadow-ambient transition-transform hover:scale-105",
        )}
      >
        <Icon name={open ? "close" : "chat"} className="text-[26px]" />
      </button>

      {/* Panel */}
      {open && (
        <div
          role="dialog"
          aria-label="PlacementPro assistant"
          className={cn(
            "fixed bottom-24 right-5 z-40 flex w-[calc(100vw-2.5rem)] max-w-sm flex-col",
            "rounded-2xl border border-outline-variant/40 bg-white shadow-ambient",
            "h-[32rem] max-h-[calc(100vh-8rem)]",
          )}
        >
          <div className="flex items-center gap-2 border-b border-outline-variant/40 px-4 py-3">
            <span className="flex h-8 w-8 items-center justify-center rounded-full bg-primary-container/10 text-primary-container">
              <Icon name="smart_toy" className="text-[20px]" />
            </span>
            <div className="min-w-0">
              <p className="text-label-md font-label-md text-on-surface">PlacementPro Assistant</p>
              <p className="truncate text-xs text-on-surface-variant">Here to help</p>
            </div>
          </div>

          <div ref={scrollRef} className="flex-1 space-y-3 overflow-y-auto px-4 py-3">
            {messages.map((m, i) => (
              <div
                key={i}
                className={cn("flex", m.role === "user" ? "justify-end" : "justify-start")}
              >
                <div
                  className={cn(
                    "max-w-[85%] whitespace-pre-wrap rounded-2xl px-3 py-2 text-body-md",
                    m.role === "user"
                      ? "bg-primary-container text-white"
                      : "bg-surface-container-low text-on-surface",
                  )}
                >
                  {m.content}
                </div>
              </div>
            ))}
            {chat.isPending && (
              <div className="flex justify-start">
                <div className="flex items-center gap-1 rounded-2xl bg-surface-container-low px-3 py-2 text-on-surface-variant">
                  <Icon name="progress_activity" className="animate-spin text-[18px]" />
                  Thinking…
                </div>
              </div>
            )}
          </div>

          <div className="border-t border-outline-variant/40 p-3">
            <div className="flex items-end gap-2">
              <textarea
                value={input}
                onChange={(e) => setInput(e.target.value)}
                onKeyDown={onKeyDown}
                rows={1}
                placeholder="Ask anything…"
                aria-label="Message"
                className={cn(
                  "max-h-28 min-h-[40px] flex-1 resize-none rounded-lg border border-surface-variant px-3 py-2",
                  "text-body-md text-on-surface placeholder-outline focus:border-primary-container",
                  "focus:outline-none focus:ring-2 focus:ring-primary-container/20",
                )}
              />
              <Button
                type="button"
                size="icon"
                onClick={() => void send()}
                isLoading={chat.isPending}
                disabled={!input.trim()}
                aria-label="Send message"
              >
                <Icon name="send" className="text-[20px]" />
              </Button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
