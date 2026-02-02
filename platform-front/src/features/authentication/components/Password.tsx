import { useState, type InputHTMLAttributes } from "react";

interface PasswordInputProps extends Omit<
  InputHTMLAttributes<HTMLInputElement>,
  "type"
> {
  label?: string;
  error?: string;
}

function Password({
  label = "Password",
  error,
  className = "",
  ...props
}: PasswordInputProps) {
  const [showPassword, setShowPassword] = useState(false);

  const togglePasswordVisibility = () => {
    setShowPassword((prev) => !prev);
  };

  return (
    <label className="flex flex-col w-full">
      <div className="flex justify-between items-baseline pb-2">
        <p className="text-[#0d1b12] dark:text-gray-200 text-sm font-medium leading-normal">
          {label}
        </p>
      </div>
      <div className="relative flex w-full items-stretch rounded-lg">
        <input
          type={showPassword ? "text" : "password"}
          className={`form-input flex w-full min-w-0 resize-none overflow-hidden rounded-lg text-[#0d1b12] dark:text-white border ${
            error ? "border-red-500" : "border-[#cfe7d7] dark:border-[#2A3C30]"
          } bg-[#f8fcf9] dark:bg-[#132018] focus:border-primary focus:ring-1 focus:ring-primary h-12 placeholder:text-[#4c9a66] dark:placeholder:text-[#3d6e50] px-4 pr-12 text-base font-normal leading-normal transition-all ${className}`}
          placeholder="Enter your password"
          aria-label={label}
          aria-invalid={!!error}
          aria-describedby={error ? "password-error" : undefined}
          {...props}
        />
        <button
          type="button"
          onClick={togglePasswordVisibility}
          className="absolute right-0 top-0 bottom-0 px-3 flex items-center justify-center text-[#4c9a66] hover:text-primary transition-colors cursor-pointer"
          aria-label={showPassword ? "Hide password" : "Show password"}
        >
          <span
            className="material-symbols-outlined"
            style={{ fontSize: "20px" }}
          >
            {showPassword ? "visibility_off" : "visibility"}
          </span>
        </button>
      </div>
      {error && (
        <p
          id="password-error"
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

export default Password;
