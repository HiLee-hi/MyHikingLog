-- step 3: summit records. run once in Supabase SQL Editor

create table summit_records (
  -- generated in the app -> same id later for offline records
  id uuid primary key,
  user_id uuid not null default auth.uid() references auth.users on delete cascade,
  -- "top100:북한산" / "forest-<mntilistno>"
  mountain_id text not null,
  -- snapshot, API mountains can't be looked up by id later
  mountain_name text not null,
  mountain_height_meters numeric,
  is_top100 boolean not null default false,
  provinces text[] not null default '{}',
  climbed_on date not null,
  course text,
  duration_minutes integer check (duration_minutes > 0),
  weather text check (weather in ('sunny', 'partly_cloudy', 'cloudy', 'rainy', 'snowy')),
  companion text check (companion in ('alone', 'friends', 'family', 'partner', 'group')),
  rating smallint check (rating between 1 and 5),
  comment text check (char_length(comment) <= 100),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  -- soft delete, kept for sync later
  deleted_at timestamptz
);

create index summit_records_user_climbed_on on summit_records (user_id, climbed_on desc);

create or replace function set_updated_at() returns trigger
language plpgsql
set search_path = ''
as $$
begin
  new.updated_at = now();
  return new;
end;
$$;

create trigger summit_records_updated_at
  before update on summit_records
  for each row execute function set_updated_at();

alter table summit_records enable row level security;

create policy "Users can manage their own summit records"
  on summit_records for all
  -- (select ...) -> evaluated once per query, not per row
  using ((select auth.uid()) = user_id)
  with check ((select auth.uid()) = user_id);
