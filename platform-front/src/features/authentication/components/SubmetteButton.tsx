import { type ButtonHTMLAttributes } from "react";

interface SubmitButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  isLoading?: boolean;
  children?: React.ReactNode;
}

function SubmitButton({
  isLoading = false,
  children = "Log In",
  disabled,
  className = "",
  ...props
}: SubmitButtonProps) {
  return (
    <button
      type="submit"
      disabled={disabled || isLoading}
      className={`mt-2 w-full bg-primary hover:bg-[#10c94d] text-[#0d1b12] text-base font-bold h-12 rounded-lg transition-colors shadow-sm flex items-center justify-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed ${className}`}
      {...props}
    >
      {isLoading ? (
        <>
          <span className="animate-spin material-symbols-outlined">
            progress_activity
          </span>
          Loading...
        </>
      ) : (
        <>
          {children}
          <span
            className="material-symbols-outlined"
            style={{ fontSize: "20px" }}
          >
            arrow_forward
          </span>
        </>
      )}
    </button>
  );
}

export default SubmitButton;
