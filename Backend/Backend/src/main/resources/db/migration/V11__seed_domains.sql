INSERT INTO domains (id, name, slug, description, created_at) VALUES
  (gen_random_uuid(), 'Technology',  'technology',  'AI, software, hardware, and the tech industry',       now()),
  (gen_random_uuid(), 'Economics',   'economics',   'Macroeconomics, monetary policy, and markets',        now()),
  (gen_random_uuid(), 'Politics',    'politics',    'Elections, policy, and geopolitics',                  now()),
  (gen_random_uuid(), 'Science',     'science',     'Research, climate, space, and medicine',              now()),
  (gen_random_uuid(), 'Sports',      'sports',      'Outcomes, records, and competitions',                 now()),
  (gen_random_uuid(), 'Business',    'business',    'Companies, startups, M&A, and leadership',            now());
