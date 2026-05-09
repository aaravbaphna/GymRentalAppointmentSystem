INSERT INTO users (name, email, role) VALUES
  ('Alice',   'alice@example.com',   'CUSTOMER'),
  ('Bob',     'bob@example.com',     'CUSTOMER'),
  ('Dr. Smith',        'smith@example.com',   'PROVIDER'),
  ('Coach Lee',        'lee@example.com',     'PROVIDER'),
  ('Admin User',       'admin@example.com',   'ADMIN'),
  ('Garp',   'carol@example.com',   'CUSTOMER'),
  ('Johnny',   'david@example.com',   'CUSTOMER'),
  ('Paris',     'eva@example.com',     'CUSTOMER'),
  ('Frank',   'frank@example.com',   'CUSTOMER'),
  ('Dr. Patel',        'patel@example.com',   'PROVIDER'),
  ('Dr. Nguyen',       'nguyen@example.com',  'PROVIDER');

INSERT INTO providers (user_id, specialty, bio) VALUES
  (3,  'General Practitioner', 'Family medicine, 10+ years'),
  (4,  'Personal Trainer',     'Strength & conditioning'),
  (10, 'Extreme Personal Trainer',        'Joint recovery and conditioning'),
  (11, 'Physical Therapist',   'Sports rehabilitation & injury recovery');

INSERT INTO services (provider_id, name, duration_min, price_cents) VALUES
  (1, 'Yoga Mat', 30, 7500),
  (1, 'Treadmill and Exercise Bike', 60, 15000),
  (2, 'Swimming Pool', 60, 9000),
  (3, 'Golf Balls and Range', 45, 11000),
  (4, 'Boxing Gloves and Punching Bag', 45, 10500);

INSERT INTO availability_slots (provider_id, service_id, start_time, end_time, status) VALUES
  (1, 1, strftime('%Y-%m-%dT09:00:00', 'now', '+1 day'), strftime('%Y-%m-%dT09:30:00', 'now', '+1 day'), 'OPEN'),
  (1, 1, strftime('%Y-%m-%dT09:30:00', 'now', '+1 day'), strftime('%Y-%m-%dT10:00:00', 'now', '+1 day'), 'OPEN'),
  (1, 1, strftime('%Y-%m-%dT10:00:00', 'now', '+1 day'), strftime('%Y-%m-%dT10:30:00', 'now', '+1 day'), 'OPEN'),
  (1, 2, strftime('%Y-%m-%dT11:00:00', 'now', '+1 day'), strftime('%Y-%m-%dT12:00:00', 'now', '+1 day'), 'OPEN'),
  (2, 3, strftime('%Y-%m-%dT08:00:00', 'now', '+1 day'), strftime('%Y-%m-%dT09:00:00', 'now', '+1 day'), 'OPEN'),
  (2, 3, strftime('%Y-%m-%dT09:00:00', 'now', '+1 day'), strftime('%Y-%m-%dT10:00:00', 'now', '+1 day'), 'OPEN'),
  (3, 4, strftime('%Y-%m-%dT08:00:00', 'now', '+1 day'), strftime('%Y-%m-%dT08:30:00', 'now', '+1 day'), 'OPEN'),
  (3, 4, strftime('%Y-%m-%dT10:00:00', 'now', '+1 day'), strftime('%Y-%m-%dT10:30:00', 'now', '+1 day'), 'OPEN'),
  (3, 5, strftime('%Y-%m-%dT11:00:00', 'now', '+1 day'), strftime('%Y-%m-%dT11:45:00', 'now', '+1 day'), 'OPEN'),
  (4, 6, strftime('%Y-%m-%dT09:00:00', 'now', '+1 day'), strftime('%Y-%m-%dT10:00:00', 'now', '+1 day'), 'OPEN'),
  (4, 7, strftime('%Y-%m-%dT10:30:00', 'now', '+1 day'), strftime('%Y-%m-%dT11:15:00', 'now', '+1 day'), 'OPEN'),
  (4, 7, strftime('%Y-%m-%dT13:00:00', 'now', '+1 day'), strftime('%Y-%m-%dT13:45:00', 'now', '+1 day'), 'OPEN');
