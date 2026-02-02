import type { InputHTMLAttributes } from "react";

interface EmailInputProps extends Omit<
  InputHTMLAttributes<HTMLInputElement>,
  "type"
> {
  label?: string;
  error?: string;
}

function Email({
  label = "Email or Username",
  error,
  className = "",
  ...props
}: EmailInputProps) {
  return (
    <label className="flex flex-col w-full">
      <p className="text-[#0d1b12] dark:text-gray-200 text-sm font-medium leading-normal pb-2">
        {label}
      </p>
      <input
        type="email"
        className={`form-input flex w-full min-w-0 resize-none overflow-hidden rounded-lg text-[#0d1b12] dark:text-white border ${
          error ? "border-red-500" : "border-[#cfe7d7] dark:border-[#2A3C30]"
        } bg-[#f8fcf9] dark:bg-[#132018] focus:border-primary focus:ring-1 focus:ring-primary h-12 placeholder:text-[#4c9a66] dark:placeholder:text-[#3d6e50] px-4 text-base font-normal leading-normal transition-all ${className}`}
        placeholder="Enter your email"
        aria-label={label}
        aria-invalid={!!error}
        aria-describedby={error ? "email-error" : undefined}
        {...props}
      />
      {error && (
        <p
          id="email-error"
          className="text-red-500 text-xs mt-2 flex items-center gap-1"
          role="alert"
        >
          <span
            className="material-symbols-outlined"
            style={{ fontSize: "14px" }}
          >
            error
          </span>
          {error}
        </p>
      )}
    </label>
  );
}

export default Email;
