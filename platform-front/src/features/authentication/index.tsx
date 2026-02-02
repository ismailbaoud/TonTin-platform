import { useState, type FormEvent } from "react";
import Header from "./components/Header";
import Divider from "./components/Divider";
import Email from "./components/Email";
import Password from "./components/Password";
import ForgetPassword from "./components/ForgetPassword";
import RegisterLink from "./components/RegistrationLink";
import SubmitButton from "./components/SubmetteButton";

interface LoginFormData {
  email: string;
  password: string;
}

interface FormErrors {
  email?: string;
  password?: string;
}

function Login() {
  const [formData, setFormData] = useState<LoginFormData>({
    email: "",
    password: "",
  });

  const [errors, setErrors] = useState<FormErrors>({});
  const [isLoading, setIsLoading] = useState(false);

  const validateEmail = (email: string): boolean => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  };

  const validateForm = (): boolean => {
    const newErrors: FormErrors = {};

    if (!formData.email.trim()) {
      newErrors.email = "Email is required";
    } else if (!validateEmail(formData.email)) {
      newErrors.email = "Please enter a valid email address";
    }

    if (!formData.password) {
      newErrors.password = "Password is required";
    } else if (formData.password.length < 6) {
      newErrors.password = "Password must be at least 6 characters";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleEmailChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData((prev) => ({ ...prev, email: e.target.value }));
    if (errors.email) {
      setErrors((prev) => ({ ...prev, email: undefined }));
    }
  };

  const handlePasswordChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData((prev) => ({ ...prev, password: e.target.value }));
    if (errors.password) {
      setErrors((prev) => ({ ...prev, password: undefined }));
    }
  };

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    console.log(formData);
    

    // setIsLoading(true);
    
  };

  return (
    <>
      <Header />
      <div className="px-8 pb-10">
        <form
          className="flex flex-col gap-5"
          onSubmit={handleSubmit}
          noValidate
        >
          <Email
            value={formData.email}
            onChange={handleEmailChange}
            error={errors.email}
            required
            autoComplete="email"
          />
          <Password
            value={formData.password}
            onChange={handlePasswordChange}
            error={errors.password}
            required
            autoComplete="current-password"
          />
          <ForgetPassword />
          <SubmitButton isLoading={isLoading} disabled={isLoading} />
        </form>
        <Divider />
        <RegisterLink />
      </div>
    </>
  );
}

export default Login;
