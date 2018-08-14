CREATE TABLE poll (
    id serial UNIQUE PRIMARY KEY,
    name varchar(60) NOT NULL,
    server_id bigint NOT NULL,
    options text[],
    end_dt timestamp NOT NULL
);

CREATE TABLE poll_vote (
    id serial UNIQUE PRIMARY KEY,
    poll_id integer NOT NULL REFERENCES poll(id),
    voter_id bigint NOT NULL,
    choice smallint NOT NULL
);
CREATE INDEX poll_vote_poll_id_idx ON poll_vote(poll_id);
