/** Если у window появится Telegram,то внутри него есть WebApp,
  а у WebApp есть initData, ready() и expand().**/
interface TelegramWebApp {
    initData: string;
    ready(): void;
    expand(): void;
}

interface Window {
    Telegram?: {
        WebApp: TelegramWebApp;
    };
}