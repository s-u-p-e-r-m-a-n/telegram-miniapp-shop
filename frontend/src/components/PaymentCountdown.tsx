import {useEffect, useState} from 'react';

type PaymentCountdownProps = {
    expiresAt: string;
};

//таймер отмены платежа
export function PaymentCountdown({expiresAt}: PaymentCountdownProps) {

    const calculateRemainingSeconds = () => {
        const expiresTime = new Date(expiresAt).getTime();
        const remainingMs = expiresTime - Date.now();
//запускаем раз в секунду вычисление оставшегося времени
        return Math.max(0, Math.floor(remainingMs / 1000));
    };

    //отрисовка таймера
    const [remainingSeconds, setRemainingSeconds] =
        useState(calculateRemainingSeconds);

    useEffect(() => {

        const intervalId = setInterval(() => {
            setRemainingSeconds(calculateRemainingSeconds());
        }, 1000);

        return () => clearInterval(intervalId);

    }, [expiresAt]);

    if (remainingSeconds === 0) {
        return <p>Время на оплату истекло</p>;
    }

    const minutes = Math.floor(remainingSeconds / 60);
    const seconds = remainingSeconds % 60;

    return (
        <p>
            Оплатите заказ в течение{' '}
            <strong>
                {minutes}:{seconds.toString().padStart(2, '0')}
            </strong>
        </p>
    );
}