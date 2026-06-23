

    const { useState, useEffect } = React;

    const ROLES = ['GOALKEEPER', 'DEFENDER', 'MIDFIELDER', 'STRIKER'];

    const ROLE_LABELS = {
    GOALKEEPER: 'Portiere',
    DEFENDER:   'Difensore',
    MIDFIELDER: 'Centrocampista',
    STRIKER:    'Attaccante',
};

    function PlayerCard({ player }) {
    const name = player.name || '';
    const surname = player.surname || '';
    const altText = `${name} ${surname}`.trim();
    const playerUrl = '/players/' + player.id;

    return (
    <div className='tournament-card'>
    <div className='tournament-card-img'>
{player.photoUrl
    ? <img src={player.photoUrl} alt={altText} />
: <span>👤</span>
}
</div>
<div className='tournament-card-body'>
    <div className='tournament-card-title'>
        <a href={playerUrl}>
            {name} {surname}
        </a>
    </div>
    <div className='tournament-card-meta'>
                        <span className='badge'>
                            {ROLE_LABELS[player.role] ?? player.role ?? 'Non specificato'}
                        </span>
        {player.teamName &&
            <span className='text-muted' style={{marginLeft: '4px'}}>
                                {player.teamName}
                            </span>
        }
    </div>
</div>
</div>
);
}

function PlayerList() {
const [players, setPlayers] = useState([]);
const [loading, setLoading] = useState(true);
const [error,   setError]   = useState(null);
const [search,  setSearch]  = useState('');
const [role,    setRole]    = useState('');

useEffect(() => {
    fetch('/api/players')
        .then(res => {
            if (!res.ok) throw new Error('Errore nel caricamento dei giocatori');
            return res.json();
        })
        .then(data => {
            // Protezione nel caso in cui l'API non restituisca un array valido
            setPlayers(Array.isArray(data) ? data : []);
            setLoading(false);
        })
        .catch(err => {
            setError(err.message);
            setLoading(false);
        });
}, []);

if (loading) return <p className='text-muted'>Caricamento giocatori...</p>;
if (error)   return <p className='text-muted'>{error}</p>;

const filtered = players.filter(p => {
    const name = p.name || '';
    const surname = p.surname || '';

    const matchesSearch = (name + ' ' + surname)
        .toLowerCase()
        .includes(search.toLowerCase());

    const matchesRole = role === '' || p.role === role;
    return matchesSearch && matchesRole;
});

return (
    <div>
        <div className='flex mb-2' style={{gap: '0.75rem', flexWrap: 'wrap'}}>
            <input
                type='text'
                placeholder='Cerca per nome...'
                value={search}
                onChange={e => setSearch(e.target.value)}
                style={{maxWidth: '260px'}}
            />
            <select
                value={role}
                onChange={e => setRole(e.target.value)}
                style={{maxWidth: '200px'}}
            >
                <option value=''>Tutti i ruoli</option>
                {ROLES.map(r => (
                    <option key={r} value={r}>{ROLE_LABELS[r]}</option>
                ))}
            </select>
            <span className='text-muted' style={{alignSelf: 'center'}}>
                        {filtered.length} giocator{filtered.length === 1 ? 'e' : 'i'}
                    </span>
        </div>

        {filtered.length === 0
            ? <p className='text-muted'>Nessun giocatore trovato.</p>
            : <div className='tournament-card-grid'>
                {filtered.map(p => <PlayerCard key={p.id} player={p} />)}
            </div>
        }
    </div>
);
}
ReactDOM.createRoot(document.getElementById('root')).render(<PlayerList />);

