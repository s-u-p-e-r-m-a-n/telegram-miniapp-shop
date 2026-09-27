import {Search} from "lucide-react";

type ProductSearchProps = {
    value: string;
    onChange: (value: string) => void;
};

export function ProductSearch({ value, onChange }: ProductSearchProps) {
    return (
        <div className="catalog-search">
            <Search size={19} />

            <input
                type="text"
                placeholder="Поиск товаров"
                value={value}
                onChange={event => onChange(event.target.value)}
            />
        </div>
    );
}