-- ============ TOURNAMENTS ============
INSERT INTO tournament (id, name, year, description) VALUES (1, 'Serie A 2025/26', 2026, 'Campionato italiano di Serie A');
INSERT INTO tournament (id, name, year, description) VALUES (2, 'Champions League 2025/26', 2026, 'Mini-girone delle 4 italiane in Champions League');

-- ============ TEAMS ============
INSERT INTO team (id, name, foundation_year, city) VALUES (1, 'AS Roma', 1927, 'Roma');
INSERT INTO team (id, name, foundation_year, city) VALUES (2, 'SS Lazio', 1900, 'Roma');
INSERT INTO team (id, name, foundation_year, city) VALUES (3, 'Juventus FC', 1897, 'Torino');
INSERT INTO team (id, name, foundation_year, city) VALUES (4, 'Inter', 1908, 'Milano');
INSERT INTO team (id, name, foundation_year, city) VALUES (5, 'AC Milan', 1899, 'Milano');

-- ============ REFEREES ============
INSERT INTO referee (id, name, surname, referee_code) VALUES (1, 'Daniele', 'Orsato', 'REF001');
INSERT INTO referee (id, name, surname, referee_code) VALUES (2, 'Marco', 'Guida', 'REF002');

-- ============ TOURNAMENT-TEAMS ============
INSERT INTO tournament_teams (tournaments_id, teams_id) VALUES (1, 1);
INSERT INTO tournament_teams (tournaments_id, teams_id) VALUES (1, 2);
INSERT INTO tournament_teams (tournaments_id, teams_id) VALUES (1, 3);
INSERT INTO tournament_teams (tournaments_id, teams_id) VALUES (1, 4);
INSERT INTO tournament_teams (tournaments_id, teams_id) VALUES (1, 5);
INSERT INTO tournament_teams (tournaments_id, teams_id) VALUES (2, 1);
INSERT INTO tournament_teams (tournaments_id, teams_id) VALUES (2, 3);
INSERT INTO tournament_teams (tournaments_id, teams_id) VALUES (2, 4);
INSERT INTO tournament_teams (tournaments_id, teams_id) VALUES (2, 5);

-- ============ PLAYERS ============
-- ROMA
INSERT INTO player (id, name, surname, birth_date, height, role, team_id) VALUES (1, 'Mile', 'Svilar', '1999-08-27', 189, 'GOALKEEPER', 1);
INSERT INTO player (id, name, surname, birth_date, height, role, team_id) VALUES (2, 'Evan', 'Ndicka', '1999-08-20', 192, 'DEFENDER', 1);
INSERT INTO player (id, name, surname, birth_date, height, role, team_id) VALUES (3, 'Manu', 'Kone', '2001-05-17', 185, 'MIDFIELDER', 1);
INSERT INTO player (id, name, surname, birth_date, height, role, team_id) VALUES (4, 'Donyell', 'Malen', '1999-01-19', 176, 'STRIKER', 1);
-- LAZIO
INSERT INTO player (id, name, surname, birth_date, height, role, team_id) VALUES (5, 'Ivan', 'Provedel', '1994-03-17', 192, 'GOALKEEPER', 2);
INSERT INTO player (id, name, surname, birth_date, height, role, team_id) VALUES (6, 'Mario', 'Gila', '2000-08-29', 184, 'DEFENDER', 2);
INSERT INTO player (id, name, surname, birth_date, height, role, team_id) VALUES (7, 'Toma', 'Basic', '1996-11-25', 188, 'MIDFIELDER', 2);
INSERT INTO player (id, name, surname, birth_date, height, role, team_id) VALUES (8, 'Pedro', 'Rodriguez', '1987-07-28', 169, 'STRIKER', 2);
-- JUVENTUS
INSERT INTO player (id, name, surname, birth_date, height, role, team_id) VALUES (9, 'Michele', 'Di Gregorio', '1997-07-27', 187, 'GOALKEEPER', 3);
INSERT INTO player (id, name, surname, birth_date, height, role, team_id) VALUES (10, 'Gleison', 'Bremer', '1997-03-18', 188, 'DEFENDER', 3);
INSERT INTO player (id, name, surname, birth_date, height, role, team_id) VALUES (11, 'Manuel', 'Locatelli', '1998-01-08', 185, 'MIDFIELDER', 3);
INSERT INTO player (id, name, surname, birth_date, height, role, team_id) VALUES (12, 'Kenan', 'Yildiz', '2005-05-04', 185, 'STRIKER', 3);
-- INTER
INSERT INTO player (id, name, surname, birth_date, height, role, team_id) VALUES (13, 'Yann', 'Sommer', '1988-12-17', 183, 'GOALKEEPER', 4);
INSERT INTO player (id, name, surname, birth_date, height, role, team_id) VALUES (14, 'Alessandro', 'Bastoni', '1999-04-13', 190, 'DEFENDER', 4);
INSERT INTO player (id, name, surname, birth_date, height, role, team_id) VALUES (15, 'Nicolo', 'Barella', '1997-02-07', 172, 'MIDFIELDER', 4);
INSERT INTO player (id, name, surname, birth_date, height, role, team_id) VALUES (16, 'Lautaro', 'Martinez', '1997-08-22', 174, 'STRIKER', 4);
-- MILAN
INSERT INTO player (id, name, surname, birth_date, height, role, team_id) VALUES (17, 'Mike', 'Maignan', '1995-07-03', 191, 'GOALKEEPER', 5);
INSERT INTO player (id, name, surname, birth_date, height, role, team_id) VALUES (18, 'Strahinja', 'Pavlovic', '2001-05-24', 194, 'DEFENDER', 5);
INSERT INTO player (id, name, surname, birth_date, height, role, team_id) VALUES (19, 'Luka', 'Modric', '1985-09-09', 172, 'MIDFIELDER', 5);
INSERT INTO player (id, name, surname, birth_date, height, role, team_id) VALUES (20, 'Christian', 'Pulisic', '1998-09-18', 178, 'STRIKER', 5);

-- ============ MATCHES SERIE A (tournament_id=1) ============
INSERT INTO match (id, date_time, local, goals_home, goals_away, state, tournament_id, home_team_id, away_team_id, referee_id)VALUES (1, '2025-09-14 20:45:00', 'Stadio Olimpico', 3, 0, 'PLAYED', 1, 1, 2, 1);
INSERT INTO match (id, date_time, local, goals_home, goals_away, state, tournament_id, home_team_id, away_team_id, referee_id)VALUES (2, '2025-09-21 20:45:00', 'Allianz Stadium', 1, 1, 'PLAYED', 1, 3, 4, 2);
INSERT INTO match (id, date_time, local, goals_home, goals_away, state, tournament_id, home_team_id, away_team_id, referee_id)VALUES (3, '2025-09-28 18:00:00', 'Stadio Olimpico', 2, 1, 'PLAYED', 1, 1, 3, 1);
INSERT INTO match (id, date_time, local, goals_home, goals_away, state, tournament_id, home_team_id, away_team_id, referee_id) VALUES (4, '2025-10-05 20:45:00', 'Stadio San Siro', 2, 2, 'PLAYED', 1, 5, 2, 2);
INSERT INTO match (id, date_time, local, goals_home, goals_away, state, tournament_id, home_team_id, away_team_id, referee_id) VALUES (5, '2025-10-19 20:45:00', 'Stadio Olimpico', 2, 0, 'PLAYED', 1, 1, 4, 1);
INSERT INTO match (id, date_time, local, goals_home, goals_away, state, tournament_id, home_team_id, away_team_id, referee_id) VALUES (6, '2025-10-26 18:00:00', 'Stadio Giuseppe Meazza', 3, 1, 'PLAYED', 1, 5, 3, 2);
INSERT INTO match (id, date_time, local, goals_home, goals_away, state, tournament_id, home_team_id, away_team_id, referee_id) VALUES (7, '2025-11-02 20:45:00', 'Stadio Olimpico', 2, 1, 'PLAYED', 1, 1, 5, 1);
INSERT INTO match (id, date_time, local, goals_home, goals_away, state, tournament_id, home_team_id, away_team_id, referee_id) VALUES (8, '2025-11-09 20:45:00', 'Stadio Olimpico', 1, 3, 'PLAYED', 1, 2, 4, 2);
INSERT INTO match (id, date_time, local, goals_home, goals_away, state, tournament_id, home_team_id, away_team_id, referee_id) VALUES (9, '2025-11-23 15:00:00', 'Allianz Stadium', 2, 0, 'PLAYED', 1, 3, 2, 1);
INSERT INTO match (id, date_time, local, goals_home, goals_away, state, tournament_id, home_team_id, away_team_id, referee_id) VALUES (10, '2025-11-30 20:45:00', 'Stadio Giuseppe Meazza', 1, 1, 'PLAYED', 1, 4, 5, 2);


INSERT INTO match (id, date_time, local, goals_home, goals_away, state, tournament_id, home_team_id, away_team_id, referee_id) VALUES (11, '2026-08-01 20:45:00', 'Stadio Olimpico', NULL, NULL, 'SCHEDULED', 1, 1, 3, 1);
INSERT INTO match (id, date_time, local, goals_home, goals_away, state, tournament_id, home_team_id, away_team_id, referee_id) VALUES (18, '2026-06-15 20:45:00', 'Stadio Olimpico', NULL, NULL, 'SCHEDULED', 1, 2, 3, 1);
INSERT INTO match (id, date_time, local, goals_home, goals_away, state, tournament_id, home_team_id, away_team_id, referee_id) VALUES (19, '2026-06-17 18:00:00', 'San Siro', NULL, NULL, 'SCHEDULED', 1, 5, 4, 2);
INSERT INTO match (id, date_time, local, goals_home, goals_away, state, tournament_id, home_team_id, away_team_id, referee_id) VALUES (20, '2026-06-20 20:45:00', 'Allianz Stadium', NULL, NULL, 'SCHEDULED', 1, 3, 1, 1);


-- ============ MATCHES CHAMPIONS LEAGUE (tournament_id=2) ============
INSERT INTO match (id, date_time, local, goals_home, goals_away, state, tournament_id, home_team_id, away_team_id, referee_id) VALUES (12, '2025-09-17 21:00:00', 'Stadio Olimpico', 2, 1, 'PLAYED', 2, 1, 3, 2);
INSERT INTO match (id, date_time, local, goals_home, goals_away, state, tournament_id, home_team_id, away_team_id, referee_id) VALUES (13, '2025-10-01 21:00:00', 'Stadio Giuseppe Meazza', 1, 1, 'PLAYED', 2, 4, 5, 1);
INSERT INTO match (id, date_time, local, goals_home, goals_away, state, tournament_id, home_team_id, away_team_id, referee_id) VALUES (14, '2025-10-22 21:00:00', 'Stadio Olimpico', 3, 1, 'PLAYED', 2, 1, 4, 1);
INSERT INTO match (id, date_time, local, goals_home, goals_away, state, tournament_id, home_team_id, away_team_id, referee_id) VALUES (15, '2025-11-05 21:00:00', 'Allianz Stadium', 0, 0, 'PLAYED', 2, 3, 5, 2);
INSERT INTO match (id, date_time, local, goals_home, goals_away, state, tournament_id, home_team_id, away_team_id, referee_id) VALUES (16, '2025-11-26 21:00:00', 'Stadio Olimpico', 2, 0, 'PLAYED', 2, 1, 5, 2);
INSERT INTO match (id, date_time, local, goals_home, goals_away, state, tournament_id, home_team_id, away_team_id, referee_id) VALUES (17, '2025-12-10 21:00:00', 'Stadio Giuseppe Meazza', 2, 1, 'PLAYED', 2, 4, 3, 1);

INSERT INTO match (id, date_time, local, goals_home, goals_away, state, tournament_id, home_team_id, away_team_id, referee_id) VALUES (21, '2026-06-22 15:00:00', 'Olimpico', NULL, NULL, 'SCHEDULED', 2, 1, 5, 2);
INSERT INTO match (id, date_time, local, goals_home, goals_away, state, tournament_id, home_team_id, away_team_id, referee_id) VALUES (22, '2026-06-25 21:00:00', 'San Siro', NULL, NULL, 'SCHEDULED', 2, 4, 2, 1);
-- ============ ALIGN SEQUENCES ============
ALTER SEQUENCE tournament_seq RESTART WITH 100;
ALTER SEQUENCE team_seq RESTART WITH 100;
ALTER SEQUENCE referee_seq RESTART WITH 100;
ALTER SEQUENCE player_seq RESTART WITH 100;
ALTER SEQUENCE match_seq RESTART WITH 100;



-- ============ CREDENTIALS ============
insert into credentials (id, username, password, role) values(0, 'matteo', '$2a$10$a6BRWVCYYD.JaDs4xeF4yeaDa0CCXOxhXhdcGU2GBxfuRNoaVylSy', 'ADMIN');









