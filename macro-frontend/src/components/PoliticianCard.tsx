import {useState} from 'react';
import {Link} from 'react-router-dom';
import {Card, CardContent} from '@/components/ui/card';
import {Badge} from '@/components/ui/badge';
import {Building2, Calendar, CheckCircle2, User} from 'lucide-react';
import {MemberSummary} from '@/models/Parliament';

interface PoliticianCardProps {
    member: MemberSummary;
    partyColor: string;
}

export const MemberAvatar = ({imageUrl, color, className, iconClassName}: {
    imageUrl: string | null;
    color: string;
    className: string;
    iconClassName: string;
}) => {
    const [failed, setFailed] = useState(false);

    return (
        <div
            className={`${className} rounded-full flex items-center justify-center flex-shrink-0 overflow-hidden`}
            style={{backgroundColor: color + '30', borderColor: color, borderWidth: 2}}
        >
            {imageUrl && !failed
                ? <img src={imageUrl} alt='' loading='lazy' className='w-full h-full object-cover'
                       onError={() => setFailed(true)}/>
                : <User className={iconClassName} style={{color}}/>}
        </div>
    );
};

const PoliticianCard = ({member, partyColor}: PoliticianCardProps) => {
    return (
        <Link to={`/parliament/politician/${member.id}`}>
            <Card
                className="glass-card hover:border-primary/50 transition-all duration-300 hover:-translate-y-1 cursor-pointer group h-full">
                <CardContent className="p-6">
                    <div className="flex items-start gap-4">
                        <MemberAvatar imageUrl={member.imageUrl} color={partyColor} className="w-16 h-16"
                                      iconClassName="w-8 h-8"/>

                        <div className="flex-1 min-w-0">
                            <h3 className="font-semibold text-lg group-hover:text-primary transition-colors truncate">
                                {member.name}
                            </h3>
                            <p className="text-muted-foreground text-sm line-clamp-2">{member.currentRole}</p>

                            <div className="flex items-center gap-2 mt-2">
                                <Badge
                                    variant="outline"
                                    className="text-xs"
                                    style={{borderColor: partyColor, color: partyColor}}
                                >
                                    {member.party || '–'}
                                </Badge>
                            </div>

                            <div className="flex flex-wrap items-center gap-x-4 gap-y-1 mt-3 text-xs text-muted-foreground">
                <span className="flex items-center gap-1">
                  <Building2 className="w-3 h-3"/>
                    {member.constituency}
                </span>
                                <span className="flex items-center gap-1">
                  <Calendar className="w-3 h-3"/>
                                    {member.yearsInParliament} år
                </span>
                                {member.attendancePct !== null && (
                                    <span className="flex items-center gap-1">
                  <CheckCircle2 className="w-3 h-3"/>
                                        {member.attendancePct}% närvaro
                </span>
                                )}
                            </div>
                        </div>
                    </div>
                </CardContent>
            </Card>
        </Link>
    );
};

export default PoliticianCard;
