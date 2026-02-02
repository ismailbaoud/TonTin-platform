import { type AnchorHTMLAttributes } from "react";

interface ForgetPasswordProps extends AnchorHTMLAttributes<HTMLAnchorElement> {
  text?: string;
  onForgotPassword?: () => void;
}

function ForgetPassword({
  text = "Forgot Password?",
  onForgotPassword,
  href = "#",
  className = "",
  ...props
}: ForgetPasswordProps) {
  const handleClick = (e: React.MouseEvent<HTMLAnchorElement>) => {
    if (onForgotPassword) {
      e.preventDefault();
      onForgotPassword();
    }
    // If props.onClick exists, call it
    props.onClick?.(e);
  };

  return (
    <div className="flex items-center justify-end">
      <a
        href={href}
        onClick={handleClick}
        className={`text-sm font-medium text-primary hover:text-[#10c94d] transition-colors ${className}`}
        {...props}
      >
        {text}
      </a>
    </div>
  );
}

export default ForgetPassword;
