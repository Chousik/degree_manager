// Seed 10-15 listings into user-service.
// Usage: USER_API_BASE=http://localhost:8888/user-service/api OWNER_ID=<uuid существующего пользователя из таблицы user> node scripts/seed-listings.js
// Node 18+ required (fetch built-in).

const USER_API_BASE = (process.env.USER_API_BASE || 'http://localhost:8888/user-service/api').replace(/\/$/, '');
const OWNER_ID = process.env.OWNER_ID;

if (!OWNER_ID) {
  console.error('OWNER_ID env var is required (uuid существующего пользователя в базе user-service/auth-service).');
  process.exit(1);
}

const sampleListings = [
  { title: 'Перфоратор Bosch GBH 2-28', pricePerHour: 8.5, depositAmount: 50, description: '850 Вт, SDS-plus, виброзащита' },
  { title: 'Аккум. шуруповерт Makita DDF482', pricePerHour: 6, depositAmount: 30, description: '18В, 2 аккумулятора, чемодан' },
  { title: 'Лазерный уровень DeWalt DW088K', pricePerHour: 7, depositAmount: 40, description: 'Красный луч, точность 0.3 мм/м' },
  { title: 'Степлер электрический Rapid', pricePerHour: 4, depositAmount: 20, description: 'Для обивки мебели и работ по дереву' },
  { title: 'Бензопила Stihl MS 180', pricePerHour: 10, depositAmount: 70, description: '38.6 см³, шина 35 см' },
  { title: 'Высоторез электрический', pricePerHour: 5, depositAmount: 25, description: 'Для обрезки веток, 710 Вт' },
  { title: 'Стремянка 6 ступеней', pricePerHour: 2, depositAmount: 10, description: 'Алюминиевая, до 120 кг' },
  { title: 'Плиткорез ручной 600 мм', pricePerHour: 3.5, depositAmount: 15, description: 'Каретка на подшипниках, длина реза 60 см' },
  { title: 'Компрессор 50 л, 2 кВт', pricePerHour: 7.5, depositAmount: 50, description: 'Для пневмоинструмента и покраски' },
  { title: 'Пароочиститель Karcher SC 2', pricePerHour: 4.5, depositAmount: 20, description: 'Пар для уборки, насадки в комплекте' },
  { title: 'Лобзик Bosch PST 900', pricePerHour: 3.5, depositAmount: 15, description: '710 Вт, мягкий ход, пилки в комплекте' },
  { title: 'Отбойный молоток 1700 Вт', pricePerHour: 12, depositAmount: 90, description: 'Шестигранник 30 мм, кейс' },
  { title: 'Тепловая пушка электрическая 3 кВт', pricePerHour: 4, depositAmount: 20, description: 'Прогрев помещений, 220В' },
  { title: 'Шлифмашина эксцентриковая Makita', pricePerHour: 4, depositAmount: 20, description: '125 мм, пылесборник' },
  { title: 'Мойка высокого давления 130 бар', pricePerHour: 6, depositAmount: 35, description: 'Для авто и двора, шланг 5 м' },
];

const withGeo = sampleListings.map((item, idx) => ({
  ...item,
  latitude: 55.7 + idx * 0.001,
  longitude: 37.5 + idx * 0.001,
  autoConfirmation: true,
}));

async function seed() {
  for (const listing of withGeo) {
    const payload = {
      ownerId: OWNER_ID,
      title: listing.title,
      description: listing.description,
      pricePerHour: listing.pricePerHour,
      depositAmount: listing.depositAmount,
      autoConfirmation: listing.autoConfirmation,
      latitude: listing.latitude,
      longitude: listing.longitude,
      availabilitySlots: [],
      photos: [],
      categoryIds: [],
    };

    const res = await fetch(`${USER_API_BASE}/listings`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });

    const text = await res.text();
    if (!res.ok) {
      console.error('Failed to create listing', listing.title, res.status, text);
      continue;
    }
    console.log('Created:', listing.title, text);
  }
}

seed().catch((err) => {
  console.error(err);
  process.exit(1);
});
