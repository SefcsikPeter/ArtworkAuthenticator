CREATE TABLE IF NOT EXISTS artwork
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(1023),
    artist ENUM(
        'Albrecht_Durer',
        'Boris_Kustodiev',
        'Camille_Pissarro',
        'Childe_Hassam',
        'Claude_Monet',
        'Edgar_Degas',
        'Eugene_Boudin',
        'Gustave_Dore',
        'Ilya_Repin',
        'Ivan_Aivazovsky',
        'Ivan_Shishkin',
        'John_Singer_Sargent',
        'Marc_Chagall',
        'Martiros_Saryan',
        'Nicholas_Roerich',
        'Pablo_Picasso',
        'Paul_Cezanne',
        'Pierre_Auguste_Renoir',
        'Pyotr_Konchalovsky',
        'Raphael_Kirchner',
        'Rembrandt',
        'Salvador_Dali',
        'Vincent_van_Gogh',
        'Alfons_Walde') NOT NULL,
    gallery VARCHAR(1023),
    price VARCHAR(255),
    description VARCHAR(4095),
    image LONGTEXT
);

CREATE TABLE IF NOT EXISTS artwork_result
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    artwork_id BIGINT NOT NULL,
    neural_net_result VARCHAR(4095),
    gpt_result LONGTEXT
)
