import { Injectable } from "@angular/core";

@Injectable({
  providedIn: "root",
})
export class ThemeService {
  private readonly storageKey = "tontin_theme";

  initTheme(): void {
    const saved = localStorage.getItem(this.storageKey);
    const prefersDark = window.matchMedia("(prefers-color-scheme: dark)").matches;
    const isDark = saved ? saved === "dark" : prefersDark;
    this.applyTheme(isDark);
  }

  setDarkMode(isDark: boolean): void {
    this.applyTheme(isDark);
    localStorage.setItem(this.storageKey, isDark ? "dark" : "light");
  }

  isDarkMode(): boolean {
    const saved = localStorage.getItem(this.storageKey);
    if (saved) {
      return saved === "dark";
    }
    return document.documentElement.classList.contains("dark");
  }

  private applyTheme(isDark: boolean): void {
    document.documentElement.classList.toggle("dark", isDark);
  }
}
