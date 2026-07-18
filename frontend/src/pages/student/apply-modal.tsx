import { Controller, useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { toast } from "sonner";
import { Modal } from "@/components/ui/modal";
import { Input } from "@/components/ui/input";
import { AiTextField } from "@/features/ai/ai-text-field";
import { Button } from "@/components/ui/button";
import { useApplyToJob } from "@/features/student/student-hooks";
import { toApiError } from "@/lib/api-helpers";
import type { EligibleJobResponse } from "@/types/domain";

const applySchema = z.object({
  resumeUrl: z.string().url("Enter a valid URL").optional().or(z.literal("")),
  coverLetter: z.string().max(5000).optional().or(z.literal("")),
});
type ApplyFormValues = z.infer<typeof applySchema>;

export function ApplyModal({
  job,
  open,
  onClose,
}: {
  job: EligibleJobResponse | null;
  open: boolean;
  onClose: () => void;
}) {
  const applyMutation = useApplyToJob();
  const {
    register,
    handleSubmit,
    reset,
    control,
    formState: { errors, isSubmitting },
  } = useForm<ApplyFormValues>({
    resolver: zodResolver(applySchema),
    defaultValues: { resumeUrl: "", coverLetter: "" },
  });

  const close = () => {
    reset();
    onClose();
  };

  const onSubmit = async (values: ApplyFormValues) => {
    if (!job) return;
    try {
      await applyMutation.mutateAsync({
        jobId: job.jobId,
        payload: {
          resumeUrl: values.resumeUrl || undefined,
          coverLetter: values.coverLetter || undefined,
        },
      });
      toast.success(`Applied to ${job.title}`);
      close();
    } catch (err) {
      toast.error(toApiError(err).message || "Unable to submit application");
    }
  };

  return (
    <Modal
      open={open}
      onClose={close}
      title={job ? `Apply to ${job.title}` : "Apply"}
      footer={
        <>
          <Button variant="outline" onClick={close} type="button">
            Cancel
          </Button>
          <Button
            type="submit"
            form="apply-form"
            isLoading={isSubmitting || applyMutation.isPending}
          >
            Submit application
          </Button>
        </>
      }
    >
      <form id="apply-form" className="space-y-4" onSubmit={handleSubmit(onSubmit)} noValidate>
        <p className="text-body-md text-on-surface-variant">
          {job?.companyName}. Attach your resume link and a short note (both optional).
        </p>
        <Input
          label="Resume URL"
          leadingIcon="link"
          placeholder="https://drive.google.com/…"
          error={errors.resumeUrl?.message}
          {...register("resumeUrl")}
        />
        <Controller
          control={control}
          name="coverLetter"
          render={({ field }) => (
            <AiTextField
              label="Cover letter"
              rows={5}
              placeholder="Why you're a great fit…"
              aiContext="cover letter"
              error={errors.coverLetter?.message}
              value={field.value ?? ""}
              onChange={field.onChange}
              onBlur={field.onBlur}
              name={field.name}
              onAiResult={(text) => field.onChange(text)}
            />
          )}
        />
      </form>
    </Modal>
  );
}
