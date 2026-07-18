import { useState } from "react";
import { toast } from "sonner";
import { Textarea, type TextareaProps } from "@/components/ui/textarea";
import { Icon } from "@/components/ui/icon";
import { cn } from "@/lib/utils";
import { toApiError } from "@/lib/api-helpers";
import { useTextAssist } from "./ai-hooks";
import type { AssistAction } from "@/types/ai";

interface AssistOption {
  action: AssistAction;
  label: string;
  icon: string;
}

const ASSIST_OPTIONS: AssistOption[] = [
  { action: "POLISH", label: "Polish", icon: "auto_fix_high" },
  { action: "SUMMARIZE", label: "Summarize", icon: "compress" },
  { action: "REWRITE_FORMAL", label: "Formal", icon: "workspace_premium" },
  { action: "REWRITE_CASUAL", label: "Casual", icon: "sentiment_satisfied" },
];

export interface AiTextFieldProps extends TextareaProps {
  /** Current field text. */
  value?: string;
  /** Called with the AI-rewritten text so the caller can update its form state. */
  onAiResult: (text: string) => void;
  /** Hint passed to the model about what this text is, e.g. "job description". */
  aiContext?: string;
}

/**
 * A {@link Textarea} with an inline AI toolbar (Polish / Summarize / Formal / Casual). Clicking an
 * action sends the current text to the assistant and replaces it in place, Gemini-style.
 */
export function AiTextField({ value, onAiResult, aiContext, ...textareaProps }: AiTextFieldProps) {
  const assist = useTextAssist();
  const [activeAction, setActiveAction] = useState<AssistAction | null>(null);

  const currentText = typeof value === "string" ? value : "";
  const disabled = assist.isPending || currentText.trim().length === 0;

  const run = async (action: AssistAction) => {
    if (currentText.trim().length === 0) {
      toast.error("Write something first, then let AI refine it.");
      return;
    }
    setActiveAction(action);
    try {
      const { result } = await assist.mutateAsync({ action, text: currentText, context: aiContext });
      onAiResult(result);
    } catch (err) {
      const apiErr = toApiError(err);
      toast.error(
        apiErr.status === 503
          ? "AI isn't configured yet. Add an OpenRouter key to enable it."
          : apiErr.message || "AI request failed",
      );
    } finally {
      setActiveAction(null);
    }
  };

  return (
    <div className="w-full">
      <Textarea value={value} {...textareaProps} />
      <div className="mt-2 flex flex-wrap items-center gap-1.5">
        <span className="flex items-center gap-1 text-xs text-on-surface-variant mr-1">
          <Icon name="magic_button" className="text-[16px]" />
          AI
        </span>
        {ASSIST_OPTIONS.map((opt) => {
          const isRunning = assist.isPending && activeAction === opt.action;
          return (
            <button
              key={opt.action}
              type="button"
              disabled={disabled}
              onClick={() => run(opt.action)}
              className={cn(
                "inline-flex items-center gap-1 rounded-full border border-surface-variant px-2.5 py-1",
                "text-xs text-on-surface transition-colors hover:bg-surface-container-low",
                "disabled:pointer-events-none disabled:opacity-50",
              )}
            >
              <Icon
                name={isRunning ? "progress_activity" : opt.icon}
                className={cn("text-[15px]", isRunning && "animate-spin")}
              />
              {opt.label}
            </button>
          );
        })}
      </div>
    </div>
  );
}
