CREATE TABLE IF NOT EXISTS owner
(
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  first_name VARCHAR(255) NOT NULL,
  last_name VARCHAR(255) NOT NULL,
  email VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS horse
(
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  description VARCHAR(4095),
  date_of_birth DATE NOT NULL,
  sex ENUM('MALE', 'FEMALE') NOT NULL,
  owner_id BIGINT
);

CREATE TABLE IF NOT EXISTS artwork
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(1023),
    artist ENUM('Albrecht_Durer','Boris_Kustodiev','Camille_Pissarro','Childe_Hassam','Claude_Monet','Edgar_Degas','Eugene_Boudin','Gustave_Dore','Ilya_Repin','Ivan_Aivazovsky','Ivan_Shishkin','John_Singer_Sargent','Marc_Chagall','Martiros_Saryan','Nicholas_Roerich','Pablo_Picasso','Paul_Cezanne','Pierre_Auguste_Renoir','Pyotr_Konchalovsky', 'Raphael_Kirchner', 'Rembrandt', 'Salvador_Dali', 'Vincent_van_Gogh', 'Alfons_Walde'),
    gallery VARCHAR(1023),
    price VARCHAR(255),
    description VARCHAR(4095),
    file_path VARCHAR(1023)
);
