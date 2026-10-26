import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, TooltipProps, XAxis, YAxis } from 'recharts';
import { SessionVotes, voteOutcomes } from '@/models/Parliament';

const total = (s: SessionVotes) => s.yes + s.no + s.abstain + s.absent;

const VotesTooltip = ({ active, payload, label }: TooltipProps<number, string>) => {
  if (!active || !payload?.length) {
    return null;
  }
  const session = payload[0].payload as SessionVotes;
  const sum = total(session);

  return (
    <div className='glass-card rounded-lg p-3 border border-border text-sm'>
      <p className='font-medium mb-2'>Riksmöte {label}</p>
      {voteOutcomes.map(o => (
        <p key={o.key} className='flex items-center gap-2'>
          <span className='w-2.5 h-2.5 rounded-full' style={{ backgroundColor: o.color }} />
          <span className='text-muted-foreground'>{o.label}</span>
          <span className='ml-auto pl-4 font-mono'>{session[o.key]}</span>
        </p>
      ))}
      <p className='flex mt-2 pt-2 border-t border-border'>
        <span className='text-muted-foreground'>Totalt</span>
        <span className='ml-auto pl-4 font-mono'>{sum}</span>
      </p>
    </div>
  );
};

const VotesOverTimeChart = ({ sessions }: { sessions: SessionVotes[] }) => (
  <figure>
    <figcaption className='text-sm text-muted-foreground mb-2'>Voteringar per riksmöte</figcaption>
    <div className='h-56' aria-hidden='true'>
      <ResponsiveContainer width='100%' height='100%'>
        <BarChart data={sessions} margin={{ top: 4, right: 4, bottom: 0, left: -16 }} barCategoryGap='20%'>
          <CartesianGrid vertical={false} strokeDasharray='3 3' stroke='hsl(var(--border))' />
          <XAxis
            dataKey='riksmote'
            stroke='hsl(var(--muted-foreground))'
            fontSize={11}
            tickLine={false}
            tickFormatter={(rm: string) => rm.slice(2)}
            interval='preserveStartEnd'
            minTickGap={8}
          />
          <YAxis stroke='hsl(var(--muted-foreground))' fontSize={11} tickLine={false} axisLine={false} allowDecimals={false} />
          <Tooltip content={<VotesTooltip />} cursor={{ fill: 'hsl(var(--muted))', opacity: 0.4 }} />
          {voteOutcomes.map((o, i) => (
            <Bar
              key={o.key}
              dataKey={o.key}
              name={o.label}
              stackId='votes'
              fill={o.color}
              stroke='hsl(var(--card))'
              strokeWidth={1}
              radius={i === voteOutcomes.length - 1 ? [4, 4, 0, 0] : 0}
              isAnimationActive={false}
            />
          ))}
        </BarChart>
      </ResponsiveContainer>
    </div>
    <table className='sr-only'>
      <caption>Voteringar per riksmöte</caption>
      <thead>
        <tr>
          <th>Riksmöte</th>
          {voteOutcomes.map(o => <th key={o.key}>{o.label}</th>)}
        </tr>
      </thead>
      <tbody>
        {sessions.map(s => (
          <tr key={s.riksmote}>
            <td>{s.riksmote}</td>
            {voteOutcomes.map(o => <td key={o.key}>{s[o.key]}</td>)}
          </tr>
        ))}
      </tbody>
    </table>
  </figure>
);

export default VotesOverTimeChart;
