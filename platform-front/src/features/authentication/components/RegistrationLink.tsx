import type { AnchorHTMLAttributes } from "react";
import { Link } from "react-router-dom";
import RegisterPage from "../../../pages/Register";

interface RegistrationLinkProps {
  text?: string;
  linkText?: string;
  href?: string;
  onRegisterClick?: () => void;
  linkProps?: AnchorHTMLAttributes<HTMLAnchorElement>;
}

function RegistrationLink({
  text = "New to TonTin?",
  linkText = "Create an account",
  href = "/register",
  onRegisterClick,
  linkProps = {},
}: RegistrationLinkProps) {
  const handleClick = (e: React.MouseEvent<HTMLAnchorElement>) => {
    if (onRegisterClick) {
      e.preventDefault();
      onRegisterClick();
    }
    // If linkProps.onClick exists, call it
    linkProps.onClick?.(e);
  };

  return (
    <div className="text-center">
      <p className="text-gray-600 dark:text-gray-400 text-sm">
        {text}
        <Link to={href} className="font-bold text-primary hover:text-[#10c94d] ml-1 hover:underline">{linkText}</Link>
      </p>
    </div>
  );
}

export default RegistrationLink;
