import { clsx, type ClassValue } from "clsx";
import { twMerge } from "tailwind-merge";

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

// Picks dark or white text for a solid background, so light party colours such as SD's yellow stay readable
export function textColorOn(hex: string): string {
  const value = hex.replace('#', '');
  if (!/^[0-9a-f]{6}$/i.test(value)) {
    return '#fff';
  }
  const channel = (i: number) => {
    const c = parseInt(value.slice(i, i + 2), 16) / 255;
    return c <= 0.03928 ? c / 12.92 : ((c + 0.055) / 1.055) ** 2.4;
  };
  const luminance = 0.2126 * channel(0) + 0.7152 * channel(2) + 0.0722 * channel(4);
  // White wins below the luminance where both choices give equal contrast
  return luminance > 0.179 ? '#111827' : '#fff';
}
